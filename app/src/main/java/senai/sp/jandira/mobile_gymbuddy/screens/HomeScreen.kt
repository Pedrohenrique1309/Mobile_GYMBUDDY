package senai.sp.jandira.mobile_gymbuddy.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import senai.sp.jandira.mobile_gymbuddy.R
import senai.sp.jandira.mobile_gymbuddy.ui.theme.MobileGYMBUDDYTheme
import senai.sp.jandira.mobile_gymbuddy.data.service.RetrofitFactory
import senai.sp.jandira.mobile_gymbuddy.data.model.Publicacao
import senai.sp.jandira.mobile_gymbuddy.data.model.ComentarioApi
import senai.sp.jandira.mobile_gymbuddy.data.model.ComentarioRequest
import kotlinx.coroutines.launch
import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.withStyle
import senai.sp.jandira.mobile_gymbuddy.utils.UserPreferences
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

// =================================================================================
// 1. MODELOS DE DADOS
// =================================================================================
data class Comment(
    val id: Int,
    val userName: String,
    val userProfileImageUrl: String?, // URL da imagem do usuário
    val text: String,
    val initialLikes: Int,
    val isInitiallyLiked: Boolean
)

data class Post(
    val id: Int,
    val userName: String,
    val userProfileImage: Int,
    val postImageUrl: String?, // URL da imagem da publicação
    val gymName: String,
    val caption: String,
    val initialLikes: Int,
    val isInitiallyLiked: Boolean,
    val comments: MutableList<Comment>
)

// =================================================================================
// 2. FUNÇÕES DE MAPEAMENTO DA API PARA UI
// =================================================================================
fun mapPublicacaoToPost(publicacao: Publicacao): Post {
    // Pega o nickname do primeiro usuário do array, ou usa um fallback
    val userName = if (publicacao.user.isNotEmpty()) {
        "@${publicacao.user[0].nickname}"
    } else {
        "@user${publicacao.idUser}"
    }
    
    return Post(
        id = publicacao.id,
        userName = userName,
        userProfileImage = R.drawable.profile_placeholder,
        postImageUrl = publicacao.imagem, // Mapear a URL da imagem da publicação
        gymName = publicacao.localizacao,
        caption = publicacao.descricao,
        initialLikes = publicacao.curtidasCount,
        isInitiallyLiked = false,
        comments = mutableListOf()
    )
}

fun mapComentarioApiToComment(comentarioApi: ComentarioApi, context: Context): Comment {
    // Pegar o nickname e foto do primeiro usuário do array, ou usar fallback
    val userName = if (comentarioApi.user.isNotEmpty()) {
        "@${comentarioApi.user[0].nickname}"
    } else {
        "@user${comentarioApi.idUser}"
    }
    
    val userProfileImageUrl = if (comentarioApi.user.isNotEmpty()) {
        comentarioApi.user[0].foto
    } else {
        null
    }
    
    Log.d("MapComment", "=== DEBUG COMENTÁRIO ===")
    Log.d("MapComment", "Comentário ID: ${comentarioApi.id}")
    Log.d("MapComment", "UserID da API: ${comentarioApi.idUser}")
    Log.d("MapComment", "UserName da API: $userName")
    Log.d("MapComment", "Foto URL: '$userProfileImageUrl'")
    Log.d("MapComment", "Foto é null? ${userProfileImageUrl == null}")
    Log.d("MapComment", "Foto é vazia? ${userProfileImageUrl?.isEmpty()}")
    Log.d("MapComment", "========================")
    
    return Comment(
        id = comentarioApi.id,
        userName = userName,
        userProfileImageUrl = userProfileImageUrl,
        text = comentarioApi.conteudo,
        initialLikes = 0, // A API não retorna likes dos comentários
        isInitiallyLiked = false
    )
}

// =================================================================================
// 4. A TELA PRINCIPAL
// =================================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val posts = remember { mutableStateListOf<Post>() }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val isDarkTheme = isSystemInDarkTheme()
    val logoRes = if (isDarkTheme) R.drawable.logo_escuro else R.drawable.logo_claro
    var selectedItem by remember { mutableStateOf<Int>(0) }
    val items = listOf("Home", "Treinos", "Conquistas", "Perfil")
    val sheetState = rememberModalBottomSheetState()
    var selectedPostForComments by remember { mutableStateOf<Post?>(null) }

    // Carregamento dos dados da API
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val service = RetrofitFactory.getPublicacaoService()
                val response = service.getPublicacoes()
                
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.status) {
                        val mappedPosts = apiResponse.publicacoes.map { mapPublicacaoToPost(it) }
                        posts.clear()
                        posts.addAll(mappedPosts)
                        errorMessage = null
                    } else {
                        errorMessage = "Erro ao carregar publicações"
                    }
                } else {
                    errorMessage = "Erro na resposta da API: ${response.code()}"
                    Log.e("HomeScreen", "Erro na API: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                errorMessage = "Erro de conexão: ${e.message}"
                Log.e("HomeScreen", "Erro de conexão", e)
            } finally {
                isLoading = false
            }
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = "Logo Gym Buddy",
                        modifier = Modifier.height(55.dp)
                    )
                },
                actions = {
                    IconButton(onClick = { /* Ação de notificação */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificações")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            // Define a cor baseada no estado de seleção do item
                            val iconColor = if (selectedItem == index) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            when (item) {
                                "Home" -> Icon(
                                    imageVector = Icons.Filled.Home,
                                    contentDescription = item,
                                    modifier = Modifier.size(28.dp),
                                    tint = iconColor // Aplica a cor dinâmica
                                )
                                "Treinos" -> Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = item,
                                    modifier = Modifier.size(28.dp),
                                    tint = iconColor // Aplica a cor dinâmica
                                )
                                "Conquistas" -> Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = item,
                                    modifier = Modifier.size(28.dp),
                                    tint = iconColor // Aplica a cor dinâmica
                                )
                                "Perfil" -> {
                                    BadgedBox(
                                        badge = {
                                            Badge { Text("3") }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Person,
                                            contentDescription = "Perfil com notificação",
                                            modifier = Modifier.size(28.dp),
                                            tint = iconColor // Aplica a cor dinâmica
                                        )
                                    }
                                }
                            }
                        },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                errorMessage != null && posts.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Erro",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                isLoading = true
                                coroutineScope.launch {
                                    try {
                                        val service = RetrofitFactory.getPublicacaoService()
                                        val response = service.getPublicacoes()
                                        
                                        if (response.isSuccessful && response.body() != null) {
                                            val apiResponse = response.body()!!
                                            if (apiResponse.status) {
                                                val mappedPosts = apiResponse.publicacoes.map { mapPublicacaoToPost(it) }
                                                posts.clear()
                                                posts.addAll(mappedPosts)
                                                errorMessage = null
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("HomeScreen", "Erro ao tentar novamente", e)
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        ) {
                            Text("Tentar Novamente")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(posts) { postData ->
                            PostItem(
                                post = postData,
                                onCommentClick = {
                                    selectedPostForComments = postData
                                }
                            )
                        }
                    }
                }
            }
        }

        if (selectedPostForComments != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedPostForComments = null },
                sheetState = sheetState
            ) {
                CommentsSheetContent(
                    post = selectedPostForComments!!,
                    onAddComment = { newCommentText ->
                        // Este callback não é mais necessário pois o CommentsSheetContent
                        // já adiciona o comentário diretamente na lista
                    }
                )
            }
        }
    }
}

// =================================================================================
// 5. COMPONENTE DO POST
// =================================================================================
@Composable
fun PostItem(
    post: Post,
    onCommentClick: () -> Unit
) {
    var isLiked by remember { mutableStateOf(post.isInitiallyLiked) }
    var likesCount by remember { mutableStateOf(post.initialLikes) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.profile_placeholder),
                contentDescription = "Foto de perfil",
                modifier = Modifier.size(40.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(post.userName, fontWeight = FontWeight.Bold)
                Text(post.gymName, fontSize = 12.sp, color = Color.Gray)
            }
        }

        AsyncImage(
            model = post.postImageUrl,
            contentDescription = "Imagem da publicação",
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.profile_placeholder),
            error = painterResource(id = R.drawable.profile_placeholder),
            onSuccess = { 
                Log.d("PostImage", "✅ Imagem da publicação carregada: ${post.postImageUrl}")
            },
            onError = { error ->
                Log.e("PostImage", "❌ Erro ao carregar imagem da publicação: ${post.postImageUrl}")
                Log.e("PostImage", "Erro: ${error.result.throwable.message}")
            },
            onLoading = {
                Log.d("PostImage", "🔄 Carregando imagem da publicação: ${post.postImageUrl}")
            }
        )

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)) {
            IconButton(onClick = {
                isLiked = !isLiked
                if (isLiked) likesCount++ else likesCount--
            }) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Curtir",
                    tint = if (isLiked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp)
                )
            }
            IconButton(onClick = { onCommentClick() }) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Comentar",
                    modifier = Modifier.size(28.dp)
                )
            }
            IconButton(onClick = { /* Enviar */ }) {
                Icon(Icons.Outlined.Send, contentDescription = "Enviar", modifier = Modifier.size(28.dp))
            }
        }

        Text(
            text = "$likesCount curtidas",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("${post.userName.substring(1)}: ")
                }
                append(post.caption)
            },
            modifier = Modifier.padding(horizontal = 12.dp),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

// =================================================================================
// 6. CONTEÚDO DA BOTTOM SHEET DE COMENTÁRIOS
// =================================================================================
@Composable
fun CommentsSheetContent(
    post: Post,
    onAddComment: (String) -> Unit
) {
    var newCommentText by remember { mutableStateOf("") }
    val isSendEnabled = newCommentText.isNotBlank()
    var isLoadingComments by remember { mutableStateOf(true) }
    var isSendingComment by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Estado local para os comentários que o Compose pode observar
    val comments = remember { mutableStateListOf<Comment>() }
    
    // Carregar comentários da API apenas uma vez
    LaunchedEffect(post.id) {
        if (comments.isEmpty()) { // Só carrega se a lista local estiver vazia
            coroutineScope.launch {
                try {
                    val comentarioService = RetrofitFactory.getComentarioService()
                    val response = comentarioService.getComentarios(post.id)
                    
                    if (response.isSuccessful && response.body() != null) {
                        val comentarioResponse = response.body()!!
                        if (comentarioResponse.status) {
                            Log.d("CommentsSheet", "=== PROCESSANDO COMENTÁRIOS DA API ===")
                            Log.d("CommentsSheet", "Total de comentários: ${comentarioResponse.comentarios.size}")
                            
                            val commentsFromApi = comentarioResponse.comentarios.map { comentarioApi ->
                                Log.d("CommentsSheet", "Processando comentário ID: ${comentarioApi.id}")
                                mapComentarioApiToComment(comentarioApi, context) 
                            }
                            
                            comments.clear()
                            comments.addAll(commentsFromApi)
                            // Também atualizar a lista do post para manter sincronizado
                            post.comments.clear()
                            post.comments.addAll(commentsFromApi)
                            Log.d("CommentsSheet", "Carregados ${commentsFromApi.size} comentários da API")
                            Log.d("CommentsSheet", "Lista local agora tem ${comments.size} comentários")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("CommentsSheet", "Erro ao carregar comentários", e)
                } finally {
                    isLoadingComments = false
                }
            }
        } else {
            isLoadingComments = false // Se já tem comentários, não precisa carregar
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        Text("Comentários", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (isLoadingComments) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn {
                    items(comments) { comment ->
                        CommentItem(comment = comment)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newCommentText,
                onValueChange = { newCommentText = it },
                label = { Text("Adicione um comentário...") },
                modifier = Modifier.weight(1f),
                enabled = !isSendingComment
            )
            IconButton(
                onClick = {
                    isSendingComment = true
                    coroutineScope.launch {
                        try {
                            val comentarioService = RetrofitFactory.getComentarioService()
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val currentDate = dateFormat.format(Date())
                            
                            // Pegar dados do usuário logado usando a classe utilitária
                            val userId = UserPreferences.getUserId(context)
                            val userNickname = UserPreferences.getUserNickname(context)
                            
                            Log.d("CommentsSheet", "Dados do usuário - ID: $userId, Nickname: $userNickname")
                            
                            val novoComentario = ComentarioRequest(
                                conteudo = newCommentText,
                                dataComentario = currentDate,
                                idPublicacao = post.id,
                                idUser = userId
                            )
                            Log.d("CommentsSheet", "Enviando comentário: $novoComentario")
                            val response = comentarioService.criarComentario(novoComentario)
                            Log.d("CommentsSheet", "Resposta: código=${response.code()}, sucesso=${response.isSuccessful}")
                            
                            if (response.isSuccessful) {
                                // Pegar foto do usuário logado (se disponível)
                                val userPhotoUrl = UserPreferences.getUserPhotoUrl(context)
                                
                                // Adicionar comentário imediatamente à lista local observável
                                val newComment = Comment(
                                    id = (0..10000).random(),
                                    userName = "@$userNickname",
                                    userProfileImageUrl = userPhotoUrl,
                                    text = newCommentText,
                                    initialLikes = 0,
                                    isInitiallyLiked = false
                                )
                                
                                // Adicionar no início da lista local (observável pelo Compose)
                                comments.add(0, newComment)
                                // Também adicionar na lista do post para manter sincronizado
                                post.comments.add(0, newComment)
                                newCommentText = ""
                                
                                Log.d("CommentsSheet", "Comentário adicionado localmente: ${newComment.userName} - ${newComment.text}")
                                onAddComment(newComment.text)
                            } else {
                            }
                        } catch (e: Exception) {
                            Log.e("CommentsSheet", "Erro ao enviar comentário", e)
                        } finally {
                            isSendingComment = false
                        }
                    }
                },
                enabled = isSendEnabled && !isSendingComment
            ) {
                if (isSendingComment) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar comentário",
                        tint = if (isSendEnabled) MaterialTheme.colorScheme.secondary else Color.Gray
                    )
                }
            }
        }
    }
}

// =================================================================================
// 7. ITEM INDIVIDUAL DE COMENTÁRIO
// =================================================================================
@Composable
fun CommentItem(comment: Comment) {
    var isLiked by remember { mutableStateOf(comment.isInitiallyLiked) }
    var likesCount by remember { mutableStateOf(comment.initialLikes) }
    
    Log.d("CommentItem", "=== RENDERIZANDO COMENTÁRIO ===")
    Log.d("CommentItem", "ID: ${comment.id}")
    Log.d("CommentItem", "UserName: ${comment.userName}")
    Log.d("CommentItem", "Foto URL: '${comment.userProfileImageUrl}'")
    Log.d("CommentItem", "Texto: ${comment.text}")
    Log.d("CommentItem", "===============================")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.weight(1f)
        ) {
            AsyncImage(
                model = comment.userProfileImageUrl,
                contentDescription = "Foto de ${comment.userName}",
                modifier = Modifier.size(32.dp).clip(CircleShape),
                placeholder = painterResource(id = R.drawable.profile_placeholder),
                error = painterResource(id = R.drawable.profile_placeholder),
                contentScale = ContentScale.Crop,
                onSuccess = { 
                    Log.d("AsyncImage", "✅ Imagem carregada com sucesso: ${comment.userProfileImageUrl}")
                },
                onError = { error ->
                    Log.e("AsyncImage", "❌ Erro ao carregar imagem: ${comment.userProfileImageUrl}")
                    Log.e("AsyncImage", "Erro: ${error.result.throwable.message}")
                },
                onLoading = {
                    Log.d("AsyncImage", "🔄 Carregando imagem: ${comment.userProfileImageUrl}")
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(comment.userName)
                        }
                        append(" ")
                        append(comment.text)
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            IconButton(
                onClick = {
                    isLiked = !isLiked
                    if (isLiked) likesCount++ else likesCount--
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Curtir comentário",
                    tint = if (isLiked) MaterialTheme.colorScheme.secondary else Color.Gray
                )
            }
            if (likesCount > 0) {
                Text(
                    text = likesCount.toString(),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// =================================================================================
// 8. PREVIEWS
// =================================================================================
@Preview(name = "Light Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun HomeScreenLightPreview() {
    MobileGYMBUDDYTheme(darkTheme = false) { HomeScreen(navController = rememberNavController()) }
}

@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenDarkPreview() {
    MobileGYMBUDDYTheme(darkTheme = true) { HomeScreen(navController = rememberNavController()) }
}