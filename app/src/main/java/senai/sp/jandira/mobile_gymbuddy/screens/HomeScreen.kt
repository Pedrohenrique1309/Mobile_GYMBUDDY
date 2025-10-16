package senai.sp.jandira.mobile_gymbuddy.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import senai.sp.jandira.mobile_gymbuddy.data.model.CurtidaRequest
import kotlinx.coroutines.launch
import android.content.Context
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
    var currentLikes: Int, // Mutável para atualizações locais
    var isCurrentlyLiked: Boolean, // Mutável para atualizações locais
    val commentsCount: Int, // Contagem de comentários do banco via trigger
    val comments: MutableList<Comment>
) {
    // Propriedades de compatibilidade
    val initialLikes: Int get() = currentLikes
    val isInitiallyLiked: Boolean get() = isCurrentlyLiked
}

// =================================================================================
// 2. FUNÇÕES DE MAPEAMENTO DA API PARA UI
// =================================================================================
fun mapPublicacaoToPost(publicacao: Publicacao, actualCommentsCount: Int = 0, actualLikesCount: Int = 0, isLikedByUser: Boolean = false): Post {
    // Pega o nickname do primeiro usuário do array, ou usa um fallback
    val userName = if (publicacao.user.isNotEmpty()) {
        "@${publicacao.user[0].nickname}"
    } else {
        "@user${publicacao.idUser}"
    }
    
    android.util.Log.d("HomeScreen", "Mapeando publicação ${publicacao.id} com ${actualCommentsCount} comentários")
    
    return Post(
        id = publicacao.id,
        userName = userName,
        userProfileImage = R.drawable.profile_placeholder,
        postImageUrl = publicacao.imagem, // Mapear a URL da imagem da publicação
        gymName = publicacao.localizacao,
        caption = publicacao.descricao,
        currentLikes = actualLikesCount, // Usar contagem real das curtidas
        isCurrentlyLiked = isLikedByUser,
        commentsCount = actualCommentsCount, // Usar contagem real dos comentários
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
    
    android.util.Log.d("MapComment", "=== DEBUG COMENTÁRIO ===")
    android.util.Log.d("MapComment", "Comentário ID: ${comentarioApi.id}")
    android.util.Log.d("MapComment", "UserID da API: ${comentarioApi.idUser}")
    android.util.Log.d("MapComment", "UserName da API: $userName")
    android.util.Log.d("MapComment", "Foto URL: '$userProfileImageUrl'")
    android.util.Log.d("MapComment", "Foto é null? ${userProfileImageUrl == null}")
    android.util.Log.d("MapComment", "Foto é vazia? ${userProfileImageUrl?.isEmpty()}")
    android.util.Log.d("MapComment", "========================")
    
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
fun HomeScreen(
    navController: NavController,
    postSuccess: Boolean = false
) {

    val posts = remember { mutableStateListOf<Post>() }
    var isLoading by remember { mutableStateOf(true) }
    var showSuccessMessage by remember { mutableStateOf(postSuccess) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Esconder mensagem de sucesso após 4 segundos
    LaunchedEffect(postSuccess) {
        if (postSuccess) {
            kotlinx.coroutines.delay(4000)
            showSuccessMessage = false
        }
    }
    
    val isDarkTheme = isSystemInDarkTheme()
    val logoRes = if (isDarkTheme) R.drawable.logo_escuro else R.drawable.logo_claro
    var selectedItem by remember { mutableStateOf<Int>(0) }
    val items = listOf(
        stringResource(R.string.nav_home),
        stringResource(R.string.nav_workouts),
        stringResource(R.string.nav_achievements),
        stringResource(R.string.nav_profile)
    )
    val sheetState = rememberModalBottomSheetState()
    var selectedPostForComments by remember { mutableStateOf<Post?>(null) }

    // Carregamento dos dados da API
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                // Buscar publicações, comentários e curtidas
                val publicacaoService = RetrofitFactory.getPublicacaoService()
                val comentarioService = RetrofitFactory.getComentarioService()
                val curtidaService = RetrofitFactory.getCurtidaService()
                
                android.util.Log.d("HomeScreen", "=== INICIANDO CARREGAMENTO ===")
                
                val publicacoesResponse = publicacaoService.getPublicacoes()
                val curtidasResponse = curtidaService.getAllCurtidas()
                
                android.util.Log.d("HomeScreen", "Publicações response: ${publicacoesResponse.isSuccessful}")
                android.util.Log.d("HomeScreen", "Curtidas response: ${curtidasResponse.isSuccessful}")
                
                if (publicacoesResponse.isSuccessful && publicacoesResponse.body() != null) {
                    val apiResponse = publicacoesResponse.body()!!
                    if (apiResponse.status) {
                        // Processar curtidas do usuário logado
                        val userId = UserPreferences.getUserId(context)
                        val userLikedPosts = mutableSetOf<Int>()
                        val curtidasCountMap = mutableMapOf<Int, Int>() // Contar curtidas por publicação
                        
                        android.util.Log.d("HomeScreen", "Processando curtidas para usuário: $userId")
                        
                        // Processar resposta de curtidas
                        if (curtidasResponse.isSuccessful && curtidasResponse.body() != null) {
                            try {
                                val curtidasApiResponse = curtidasResponse.body()!!
                                android.util.Log.d("HomeScreen", "Curtidas API Response status: ${curtidasApiResponse.status}")
                                
                                if (curtidasApiResponse.status && curtidasApiResponse.curtidas != null) {
                                    android.util.Log.d("HomeScreen", "Total curtidas na API: ${curtidasApiResponse.curtidas.size}")
                                    
                                    curtidasApiResponse.curtidas.forEach { curtida ->
                                        if (curtida.user.isNotEmpty() && curtida.publicacao.isNotEmpty()) {
                                            val curtidaUserId = curtida.user[0].id
                                            val curtidaPostId = curtida.publicacao[0].id
                                            
                                            android.util.Log.d("HomeScreen", "Curtida: User $curtidaUserId curtiu Post $curtidaPostId")
                                            
                                            // Contar curtidas por publicação
                                            curtidasCountMap[curtidaPostId] = curtidasCountMap.getOrDefault(curtidaPostId, 0) + 1
                                            
                                            // Verificar se o usuário atual curtiu este post
                                            if (curtidaUserId == userId) {
                                                userLikedPosts.add(curtidaPostId)
                                                android.util.Log.d("HomeScreen", "✅ Usuário atual curtiu post $curtidaPostId")
                                            }
                                        }
                                    }
                                } else {
                                    android.util.Log.w("HomeScreen", "Status false ou curtidas null")
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("HomeScreen", "Erro ao processar curtidas", e)
                            }
                        } else {
                            android.util.Log.e("HomeScreen", "Erro na resposta de curtidas: ${curtidasResponse.code()}")
                            try {
                                android.util.Log.e("HomeScreen", "Error body: ${curtidasResponse.errorBody()?.string()}")
                            } catch (e: Exception) {
                                android.util.Log.e("HomeScreen", "Erro ao ler error body", e)
                            }
                        }
                        
                        android.util.Log.d("HomeScreen", "Posts curtidos pelo usuário: $userLikedPosts")
                        android.util.Log.d("HomeScreen", "Contagem de curtidas por post: $curtidasCountMap")
                        
                        // Contar comentários por publicação individualmente
                        val commentsCountMap = mutableMapOf<Int, Int>()
                        
                        // Para cada publicação, buscar seus comentários
                        apiResponse.publicacoes.forEach { publicacao ->
                            try {
                                val comentariosResponse = comentarioService.getComentarios(publicacao.id)
                                if (comentariosResponse.isSuccessful && comentariosResponse.body() != null) {
                                    val comentariosApiResponse = comentariosResponse.body()!!
                                    if (comentariosApiResponse.status) {
                                        android.util.Log.d("HomeScreen", "🔍 DEBUG FILTRO - Publicação ${publicacao.id}:")
                                        android.util.Log.d("HomeScreen", "  📦 Total comentários da API: ${comentariosApiResponse.comentarios.size}")
                                        
                                        // Log detalhado ANTES do filtro
                                        comentariosApiResponse.comentarios.forEachIndexed { index, comentario ->
                                            val publicacaoDoComentario = if (comentario.publicacao.isNotEmpty()) comentario.publicacao[0].id else "null"
                                            val pertence = comentario.publicacao.isNotEmpty() && comentario.publicacao[0].id == publicacao.id
                                            android.util.Log.d("HomeScreen", "  📝 Comentário $index: ID=${comentario.id}, pertence à pub=${publicacaoDoComentario}, desejada=${publicacao.id}")
                                            android.util.Log.d("HomeScreen", "      Filtrar? ${if (pertence) "✅ SIM" else "❌ NÃO"}")
                                        }
                                        
                                        // FILTRAR usando o ID da publicação do array publicacao
                                        val comentariosFiltrados = comentariosApiResponse.comentarios.filter { comentario ->
                                            // Verificar se o comentário pertence a esta publicação usando o array publicacao
                                            comentario.publicacao.isNotEmpty() && comentario.publicacao[0].id == publicacao.id
                                        }
                                        
                                        val count = comentariosFiltrados.size
                                        commentsCountMap[publicacao.id] = count
                                        
                                        android.util.Log.d("HomeScreen", "  ✅ Resultado: ${count} comentários para publicação ${publicacao.id}")
                                    }
                                } else {
                                    android.util.Log.w("HomeScreen", "Erro ao buscar comentários da publicação ${publicacao.id}")
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("HomeScreen", "Erro ao buscar comentários da publicação ${publicacao.id}: ${e.message}")
                                commentsCountMap[publicacao.id] = 0
                            }
                        }
                        
                        android.util.Log.d("HomeScreen", "Mapa final de contagem: $commentsCountMap")
                        
                        val mappedPosts = apiResponse.publicacoes.map { publicacao ->
                            val isLiked = userLikedPosts.contains(publicacao.id)
                            val realLikesCount = curtidasCountMap.getOrDefault(publicacao.id, 0)
                            android.util.Log.d("HomeScreen", "Post ${publicacao.id}: DB_curtidas=${publicacao.curtidasCount}, REAL_curtidas=$realLikesCount, isLiked=$isLiked")
                            
                            mapPublicacaoToPost(
                                publicacao = publicacao,
                                actualCommentsCount = commentsCountMap.getOrDefault(publicacao.id, 0),
                                actualLikesCount = realLikesCount, // Contagem real baseada na API de curtidas
                                isLikedByUser = isLiked // Estado real das curtidas do servidor
                            )
                        }
                        posts.clear()
                        posts.addAll(mappedPosts)
                        errorMessage = null
                        android.util.Log.d("HomeScreen", "Posts carregados: ${posts.size}")
                    } else {
                        errorMessage = "Erro ao carregar publicações"
                        android.util.Log.e("HomeScreen", "Status falso na resposta da API")
                    }
                } else {
                    errorMessage = "Erro na resposta da API"
                    android.util.Log.e("HomeScreen", "Erro na resposta: ${publicacoesResponse.code()}")
                }
            } catch (e: Exception) {
                errorMessage = "Erro de conexão. Tente novamente."
                android.util.Log.e("HomeScreen", "Erro de conexão", e)
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
                        contentDescription = stringResource(R.string.logo_gym_buddy),
                        modifier = Modifier.height(75.dp)
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("notifications") }) {
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    navController.navigate("publishing")
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nova publicação",
                    modifier = Modifier.size(24.dp)
                )
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
                                // Usar a mesma lógica do LaunchedEffect para manter consistência
                                isLoading = true
                                coroutineScope.launch {
                                    try {
                                        val publicacaoService = RetrofitFactory.getPublicacaoService()
                                        val comentarioService = RetrofitFactory.getComentarioService()
                                        val curtidaService = RetrofitFactory.getCurtidaService()
                                        
                                        val publicacoesResponse = publicacaoService.getPublicacoes()
                                        val curtidasResponse = curtidaService.getAllCurtidas()
                                        
                                        if (publicacoesResponse.isSuccessful && publicacoesResponse.body() != null) {
                                            val apiResponse = publicacoesResponse.body()!!
                                            if (apiResponse.status) {
                                                // Processar curtidas do usuário logado
                                                val userId = UserPreferences.getUserId(context)
                                                val userLikedPosts = mutableSetOf<Int>()
                                                val curtidasCountMap = mutableMapOf<Int, Int>()
                                                
                                                // Processar curtidas
                                                if (curtidasResponse.isSuccessful && curtidasResponse.body() != null) {
                                                    val curtidasApiResponse = curtidasResponse.body()!!
                                                    if (curtidasApiResponse.status && curtidasApiResponse.curtidas != null) {
                                                        curtidasApiResponse.curtidas.forEach { curtida ->
                                                            if (curtida.user.isNotEmpty() && curtida.publicacao.isNotEmpty()) {
                                                                val curtidaUserId = curtida.user[0].id
                                                                val curtidaPostId = curtida.publicacao[0].id
                                                                
                                                                curtidasCountMap[curtidaPostId] = curtidasCountMap.getOrDefault(curtidaPostId, 0) + 1
                                                                
                                                                if (curtidaUserId == userId) {
                                                                    userLikedPosts.add(curtidaPostId)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                
                                                // Contar comentários por publicação individualmente (mesma lógica)
                                                val commentsCountMap = mutableMapOf<Int, Int>()
                                                apiResponse.publicacoes.forEach { publicacao ->
                                                    try {
                                                        val comentariosResponse = comentarioService.getComentarios(publicacao.id)
                                                        if (comentariosResponse.isSuccessful && comentariosResponse.body() != null) {
                                                            val comentariosApiResponse = comentariosResponse.body()!!
                                                            if (comentariosApiResponse.status) {
                                                                val count = comentariosApiResponse.comentarios.size
                                                                commentsCountMap[publicacao.id] = count
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        commentsCountMap[publicacao.id] = 0
                                                    }
                                                }
                                                
                                                val mappedPosts = apiResponse.publicacoes.map { publicacao ->
                                                    val isLiked = userLikedPosts.contains(publicacao.id)
                                                    val realLikesCount = curtidasCountMap.getOrDefault(publicacao.id, 0)
                                                    
                                                    mapPublicacaoToPost(
                                                        publicacao = publicacao,
                                                        actualCommentsCount = commentsCountMap.getOrDefault(publicacao.id, 0),
                                                        actualLikesCount = realLikesCount,
                                                        isLikedByUser = isLiked
                                                    )
                                                }
                                                posts.clear()
                                                posts.addAll(mappedPosts)
                                                errorMessage = null
                                            }
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("HomeScreen", "Erro ao tentar novamente", e)
                                        errorMessage = "Erro de conexão. Tente novamente."
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        ) {
                            Text(stringResource(R.string.try_again))
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Card de sucesso no topo
                        if (showSuccessMessage) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF4CAF50) // Verde
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = stringResource(R.string.success_icon),
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.post_success_message),
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                        
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
    // Usar os estados mutáveis do próprio objeto Post
    var isLiked by remember(post.id) { mutableStateOf(post.isCurrentlyLiked) }
    var likesCount by remember(post.id) { mutableStateOf(post.currentLikes) }
    
    // Sincronizar com o objeto Post quando ele mudar
    LaunchedEffect(post.isCurrentlyLiked, post.currentLikes) {
        isLiked = post.isCurrentlyLiked
        likesCount = post.currentLikes
    }
    // Usar estado derivado que sempre reflete o tamanho atual da lista
    val commentsCount by remember { 
        derivedStateOf { maxOf(post.commentsCount, post.comments.size) }
    }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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
            placeholder = painterResource(id = R.drawable.img),
            error = painterResource(id = R.drawable.img),
            onSuccess = { 
                android.util.Log.d("PostImage", "✅ Imagem da publicação carregada: ${post.postImageUrl}")
            },
            onError = { error ->
                android.util.Log.e("PostImage", "❌ Erro ao carregar imagem da publicação: ${post.postImageUrl}")
                android.util.Log.e("PostImage", "Erro: ${error.result.throwable.message}")
            },
            onLoading = {
                android.util.Log.d("PostImage", "🔄 Carregando imagem da publicação: ${post.postImageUrl}")
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Botão de curtir com contador
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        try {
                            val curtidaService = RetrofitFactory.getCurtidaService()
                            val userId = UserPreferences.getUserId(context)
                            
                            // Curtir/Descurtir usando o mesmo endpoint (API decide baseado no estado atual)
                            val curtidaRequest = CurtidaRequest(
                                idUser = userId,
                                idPublicacao = post.id
                            )
                            
                            android.util.Log.d("PostItem", "Enviando curtida request: $curtidaRequest")
                            android.util.Log.d("PostItem", "Estado atual: isLiked=$isLiked")
                            
                            val response = curtidaService.adicionarCurtida(curtidaRequest)
                            
                            if (response.isSuccessful) {
                                android.util.Log.d("PostItem", "Curtida processada com sucesso")
                            } else {
                                android.util.Log.e("PostItem", "Erro ao processar curtida: ${response.code()}")
                                try {
                                    android.util.Log.e("PostItem", "Error body: ${response.errorBody()?.string()}")
                                } catch (e: Exception) {
                                    android.util.Log.e("PostItem", "Erro ao ler error body", e)
                                }
                                return@launch
                            }
                            
                            // Atualizar UI localmente e o objeto Post
                            isLiked = !isLiked
                            if (isLiked) {
                                likesCount++
                                post.currentLikes++
                                post.isCurrentlyLiked = true
                            } else {
                                likesCount--
                                post.currentLikes--
                                post.isCurrentlyLiked = false
                            }
                            
                            android.util.Log.d("PostItem", "Post ${post.id} - isLiked: $isLiked, likes: $likesCount")
                            
                        } catch (e: Exception) {
                            android.util.Log.e("PostItem", "Erro ao processar curtida", e)
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Curtir",
                    tint = if (isLiked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp)
                )
                if (likesCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.likes_count_compact, likesCount),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            
            // Botão de comentar com contador
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onCommentClick() }
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Comentar",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
                if (commentsCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.comments_count_compact, commentsCount),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                android.util.Log.d("PostItem", "Post ${post.id} tem ${commentsCount} comentários (${post.comments.size} na lista)")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
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
                            android.util.Log.d("CommentsSheet", "=== PROCESSANDO COMENTÁRIOS DA API ===")
                            android.util.Log.d("CommentsSheet", "Total de comentários: ${comentarioResponse.comentarios.size}")
                            
                            android.util.Log.d("CommentsSheet", "📦 Total comentários da API: ${comentarioResponse.comentarios.size}")
                            android.util.Log.d("CommentsSheet", "🔍 Publicação ID: ${post.id}")
                            
                            // Log dos comentários para debug
                            comentarioResponse.comentarios.forEachIndexed { index, comentario ->
                                val publicacaoDoComentario = if (comentario.publicacao.isNotEmpty()) comentario.publicacao[0].id else "null"
                                val pertence = comentario.publicacao.isNotEmpty() && comentario.publicacao[0].id == post.id
                                android.util.Log.d("CommentsSheet", "  📝 Comentário $index: ID=${comentario.id}, pertence à pub=${publicacaoDoComentario}, post=${post.id}")
                                android.util.Log.d("CommentsSheet", "      Filtrar? ${if (pertence) "✅ SIM" else "❌ NÃO"}")
                            }
                            
                            // FILTRAR comentários para mostrar apenas os desta publicação específica
                            val comentariosFiltrados = comentarioResponse.comentarios.filter { comentarioApi ->
                                // Verificar se o comentário realmente pertence a esta publicação usando o array publicacao
                                comentarioApi.publicacao.isNotEmpty() && comentarioApi.publicacao[0].id == post.id
                            }
                            android.util.Log.d("CommentsSheet", "✅ Comentários a serem exibidos: ${comentariosFiltrados.size}")
                            
                            val commentsFromApi = comentariosFiltrados.map { comentarioApi ->
                                android.util.Log.d("CommentsSheet", "Processando comentário ID: ${comentarioApi.id}")
                                mapComentarioApiToComment(comentarioApi, context) 
                            }
                            
                            comments.clear()
                            comments.addAll(commentsFromApi)
                            // Também atualizar a lista do post para manter sincronizado
                            post.comments.clear()
                            post.comments.addAll(commentsFromApi)
                            android.util.Log.d("CommentsSheet", "Carregados ${commentsFromApi.size} comentários da API")
                            android.util.Log.d("CommentsSheet", "Lista local agora tem ${comments.size} comentários")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CommentsSheet", "Erro ao carregar comentários", e)
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
                            
                            android.util.Log.d("CommentsSheet", "Dados do usuário - ID: $userId, Nickname: $userNickname")
                            
                            val novoComentario = ComentarioRequest(
                                conteudo = newCommentText,
                                dataComentario = currentDate,
                                idPublicacao = post.id,
                                idUser = userId
                            )
                            android.util.Log.d("CommentsSheet", "Enviando comentário: $novoComentario")
                            val response = comentarioService.criarComentario(novoComentario)
                            android.util.Log.d("CommentsSheet", "Resposta: código=${response.code()}, sucesso=${response.isSuccessful}")
                            
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
                                
                                android.util.Log.d("CommentsSheet", "Comentário adicionado localmente: ${newComment.userName} - ${newComment.text}")
                                onAddComment(newComment.text)
                            } else {
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("CommentsSheet", "Erro ao enviar comentário", e)
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
                        imageVector = Icons.Default.ArrowForward,
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
    
    android.util.Log.d("CommentItem", "=== RENDERIZANDO COMENTÁRIO ===")
    android.util.Log.d("CommentItem", "ID: ${comment.id}")
    android.util.Log.d("CommentItem", "UserName: ${comment.userName}")
    android.util.Log.d("CommentItem", "Foto URL: '${comment.userProfileImageUrl}'")
    android.util.Log.d("CommentItem", "Texto: ${comment.text}")
    android.util.Log.d("CommentItem", "===============================")

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
                    android.util.Log.d("AsyncImage", "✅ Imagem carregada com sucesso: ${comment.userProfileImageUrl}")
                },
                onError = { error ->
                    android.util.Log.e("AsyncImage", "❌ Erro ao carregar imagem: ${comment.userProfileImageUrl}")
                    android.util.Log.e("AsyncImage", "Erro: ${error.result.throwable.message}")
                },
                onLoading = {
                    android.util.Log.d("AsyncImage", "🔄 Carregando imagem: ${comment.userProfileImageUrl}")
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