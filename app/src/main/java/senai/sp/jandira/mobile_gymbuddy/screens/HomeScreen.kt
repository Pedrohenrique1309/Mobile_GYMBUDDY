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
import android.util.Log
import androidx.compose.ui.text.withStyle
import java.text.SimpleDateFormat
import java.util.*

// =================================================================================
// 1. MODELOS DE DADOS
// =================================================================================
data class Comment(
    val id: Int,
    val userName: String,
    val userProfileImage: Int,
    val text: String,
    val initialLikes: Int,
    val isInitiallyLiked: Boolean
)

data class Post(
    val id: Int,
    val userName: String,
    val userProfileImage: Int,
    val gymName: String,
    val caption: String,
    val initialLikes: Int,
    val isInitiallyLiked: Boolean,
    val comments: MutableList<Comment>
)

// =================================================================================
// 2. DADOS PROVISÓRIOS
// =================================================================================
val mockApiData = listOf(
    Post(
        id = 1, userName = "@TreinadorJonas", userProfileImage = R.drawable.profile_placeholder,
        gymName = "Academia BlaBlaBla", caption = "O de hoje tá feito, um passo de cada vez! 🙏",
        initialLikes = 132, isInitiallyLiked = true,
        comments = mutableListOf(
            Comment(1, "@MailtonJose", R.drawable.profile_placeholder, "É isso aí, mestre!", 15, true),
            Comment(2, "@AnaFitness", R.drawable.profile_placeholder, "Boraaa! 💪", 2, false)
        )
    ),
    Post(
        id = 2, userName = "@MailtonJose", userProfileImage = R.drawable.profile_placeholder,
        gymName = "Academia Body Space", caption = "Projeto verão continua firme! #foco",
        initialLikes = 254, isInitiallyLiked = false,
        comments = mutableListOf(
            Comment(3, "@TreinadorJonas", R.drawable.profile_placeholder, "Continue focado!", 8, false)
        )
    ),
    Post(
        id = 3, userName = "@AnaFitness", userProfileImage = R.drawable.profile_placeholder,
        gymName = "Gym Power", caption = "Novo recorde pessoal no agachamento! 💪",
        initialLikes = 589, isInitiallyLiked = false,
        comments = mutableListOf()
    )
)

// =================================================================================
// 3. FUNÇÕES DE MAPEAMENTO DA API PARA UI
// =================================================================================
fun mapPublicacaoToPost(publicacao: Publicacao): Post {
    return Post(
        id = publicacao.id,
        userName = "@user${publicacao.idUser}", // Nome baseado no ID do usuário
        userProfileImage = R.drawable.profile_placeholder,
        gymName = publicacao.localizacao,
        caption = publicacao.descricao,
        initialLikes = publicacao.curtidasCount,
        isInitiallyLiked = false,
        comments = mutableListOf()
    )
}

fun mapComentarioApiToComment(comentarioApi: ComentarioApi): Comment {
    return Comment(
        id = comentarioApi.id,
        userName = "@user${comentarioApi.idUser}", // Nome baseado no ID do usuário
        userProfileImage = R.drawable.profile_placeholder,
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
                // Fallback para dados mockados em caso de erro
                posts.clear()
                posts.addAll(mockApiData)
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
                        val newComment = Comment(
                            id = (0..10000).random(),
                            userName = "@UsuarioLogado",
                            userProfileImage = R.drawable.profile_placeholder,
                            text = newCommentText,
                            initialLikes = 0,
                            isInitiallyLiked = false
                        )
                        selectedPostForComments?.comments?.add(newComment)
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

        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(MaterialTheme.colorScheme.surfaceVariant))

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
    
    // Carregar comentários da API
    LaunchedEffect(post.id) {
        coroutineScope.launch {
            try {
                val comentarioService = RetrofitFactory.getComentarioService()
                val response = comentarioService.getComentarios(post.id)
                
                if (response.isSuccessful && response.body() != null) {
                    val comentarioResponse = response.body()!!
                    if (comentarioResponse.status) {
                        val commentsFromApi = comentarioResponse.comentarios.map { 
                            mapComentarioApiToComment(it) 
                        }
                        post.comments.clear()
                        post.comments.addAll(commentsFromApi)
                    }
                }
            } catch (e: Exception) {
                Log.e("CommentsSheet", "Erro ao carregar comentários", e)
            } finally {
                isLoadingComments = false
            }
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
                    items(post.comments) { comment ->
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
                            
                            val novoComentario = ComentarioRequest(
                                conteudo = newCommentText,
                                dataComentario = currentDate,
                                idPublicacao = post.id,
                                idUser = 1 // TODO: Pegar ID do usuário logado
                            )
                            
                            Log.d("CommentsSheet", "Enviando comentário: $novoComentario")
                            val response = comentarioService.criarComentario(novoComentario)
                            Log.d("CommentsSheet", "Resposta: código=${response.code()}, sucesso=${response.isSuccessful}")
                            
                            if (response.isSuccessful) {
                                // Adicionar comentário localmente
                                val newComment = Comment(
                                    id = (0..10000).random(),
                                    userName = "@user1", // TODO: Pegar nome do usuário logado
                                    userProfileImage = R.drawable.profile_placeholder,
                                    text = newCommentText,
                                    initialLikes = 0,
                                    isInitiallyLiked = false
                                )
                                post.comments.add(newComment)
                                newCommentText = ""
                                onAddComment(newComment.text)
                            } else {
                                Log.e("CommentsSheet", "Erro ao enviar comentário: ${response.code()}")
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

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(id = comment.userProfileImage),
                contentDescription = "Foto de ${comment.userName}",
                modifier = Modifier.size(32.dp).clip(CircleShape)
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