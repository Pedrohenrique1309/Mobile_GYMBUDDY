package senai.sp.jandira.mobile_gymbuddy.screens

import android.content.Context
import android.content.res.Configuration
import android.util.Log
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
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import senai.sp.jandira.mobile_gymbuddy.R
import senai.sp.jandira.mobile_gymbuddy.data.model.*
import senai.sp.jandira.mobile_gymbuddy.data.service.RetrofitFactory
import senai.sp.jandira.mobile_gymbuddy.ui.theme.MobileGYMBUDDYTheme
import senai.sp.jandira.mobile_gymbuddy.utils.UserPreferences

// =================================================================================
// 1. MODELOS DE DADOS OTIMIZADOS
// =================================================================================

/**
 * Modelo otimizado para UI que aproveita os dados da view vw_feed_publicacoes
 */
data class PostOptimized(
    val id: Int,
    val userName: String,
    val userProfileImageUrl: String?,
    val postImageUrl: String,
    val gymName: String?,
    val caption: String,
    val curtidasCount: Int,
    val comentariosCount: Int,
    val isLiked: Boolean = false
)

// =================================================================================
// 2. FUNÇÕES DE MAPEAMENTO OTIMIZADAS
// =================================================================================

/**
 * Mapeia FeedPublicacao (da view do banco) para PostOptimized (UI)
 */
suspend fun mapFeedPublicacaoToPostOptimized(feedPublicacao: FeedPublicacao, context: Context): PostOptimized {
    // Verificar se o usuário atual curtiu esta publicação
    val isLiked = checkIfUserLikedPost(feedPublicacao.idPublicacao, context)
    
    return PostOptimized(
        id = feedPublicacao.idPublicacao,
        userName = "@${feedPublicacao.nomeUsuario}",
        userProfileImageUrl = feedPublicacao.fotoPerfil,
        postImageUrl = feedPublicacao.imagem,
        gymName = feedPublicacao.localizacao,
        caption = feedPublicacao.descricao,
        curtidasCount = feedPublicacao.curtidasCount,
        comentariosCount = feedPublicacao.comentariosCount,
        isLiked = isLiked
    )
}

/**
 * Verifica se o usuário atual curtiu uma publicação específica
 */
suspend fun checkIfUserLikedPost(postId: Int, context: Context): Boolean {
    Log.d("CURTIDA_DEBUG", "============ CHECK IF USER LIKED POST ============")
    return try {
        val userId = UserPreferences.getUserId(context)
        val curtidaService = RetrofitFactory.getCurtidaService()
        
        Log.d("CURTIDA_DEBUG", "🔍 Verificando se usuário curtiu:")
        Log.d("CURTIDA_DEBUG", "   📝 PostID: $postId")
        Log.d("CURTIDA_DEBUG", "   👤 UserID: $userId")
        Log.d("CURTIDA_DEBUG", "📡 Chamando listarCurtida()...")
        
        val response = curtidaService.listarCurtida()
        
        Log.d("CURTIDA_DEBUG", "📊 Resposta listarCurtida:")
        Log.d("CURTIDA_DEBUG", "   📋 Code: ${response.code()}")
        Log.d("CURTIDA_DEBUG", "   ✅ Successful: ${response.isSuccessful}")
        
        if (response.isSuccessful) {
            val curtidaResponse = response.body()
            val curtidas = curtidaResponse?.curtidas ?: emptyList()
            
            Log.d("CURTIDA_DEBUG", "📋 Total de curtidas recebidas: ${curtidas.size}")
            
            // Procurar se existe curtida deste usuário nesta publicação
            val curtidaEncontrada = curtidas.find { curtida ->
                curtida.idUser == userId && curtida.idPublicacao == postId 
            }
            
            val isCurtido = curtidaEncontrada != null
            Log.d("CURTIDA_DEBUG", "✅ Resultado:")
            Log.d("CURTIDA_DEBUG", "   ❤️ Usuário curtiu: $isCurtido")
            Log.d("CURTIDA_DEBUG", "   📊 Curtida encontrada: $curtidaEncontrada")
            
            isCurtido
        } else {
            val errorBody = response.errorBody()?.string()
            Log.e("CURTIDA_DEBUG", "❌ ERRO ao listar curtidas:")
            Log.e("CURTIDA_DEBUG", "   📋 Code: ${response.code()}")
            Log.e("CURTIDA_DEBUG", "   📋 Error: $errorBody")
            false
        }
    } catch (e: Exception) {
        Log.e("CURTIDA_DEBUG", "💥 EXCEPTION em checkIfUserLikedPost:")
        Log.e("CURTIDA_DEBUG", "   📋 Tipo: ${e::class.java.simpleName}")
        Log.e("CURTIDA_DEBUG", "   📋 Mensagem: ${e.message}", e)
        false
    } finally {
        Log.d("CURTIDA_DEBUG", "============ FIM CHECK USER LIKED ============")
    }
}

/**
 * Encontra o ID de uma curtida específica do usuário em uma publicação
 */
suspend fun findCurtidaId(postId: Int, userId: Int): Int? {
    return try {
        val curtidaService = RetrofitFactory.getCurtidaService()
        val response = curtidaService.listarCurtida()
        
        Log.d("CURTIDA_DEBUG", "🔍 Listando curtidas para encontrar ID...")
        
        if (response.isSuccessful) {
            val curtidaResponse = response.body()
            val curtidas = curtidaResponse?.curtidas ?: emptyList()
            
            Log.d("CURTIDA_DEBUG", "📋 Total curtidas: ${curtidas.size}")
            
            val curtida = curtidas.find { curtidaItem ->
                curtidaItem.idUser == userId && curtidaItem.idPublicacao == postId 
            }
            
            Log.d("CURTIDA_DEBUG", "🎯 Curtida encontrada: $curtida")
            curtida?.id
        } else {
            Log.e("CURTIDA_DEBUG", "❌ Erro ao listar curtidas: ${response.code()}")
            null
        }
    } catch (e: Exception) {
        Log.e("CURTIDA_DEBUG", "💥 Erro ao buscar ID da curtida: ${e.message}", e)
        null
    }
}

/**
 * Implementação das curtidas usando os endpoints fornecidos
 */
suspend fun toggleLike(postId: Int, isLiked: Boolean, context: Context): Int {
    return try {
        val userId = UserPreferences.getUserId(context)
        val curtidaService = RetrofitFactory.getCurtidaService()
        
        Log.d("CURTIDA_API", "=== TOGGLE CURTIDA ===")
        Log.d("CURTIDA_API", "Post: $postId, User: $userId, IsLiked: $isLiked")
        
        val result = if (isLiked) {
            // ADICIONAR CURTIDA - POST /v1/gymbuddy/curtida
            Log.d("CURTIDA_API", "➕ Adicionando curtida...")
            val request = CurtidaPublicacaoRequest(
                idPublicacao = postId,
                idUser = userId
            )
            
            val response = curtidaService.inserirCurtida(request)
            Log.d("CURTIDA_API", "POST Response: ${response.code()}")
            
            if (response.isSuccessful) {
                Log.d("CURTIDA_API", "✅ Curtida adicionada com sucesso!")
                true
            } else {
                Log.e("CURTIDA_API", "❌ Erro ao adicionar curtida: ${response.errorBody()?.string()}")
                false
            }
        } else {
            // REMOVER CURTIDA - DELETE /v1/gymbuddy/curtida/{search_id}
            Log.d("CURTIDA_API", "➖ Removendo curtida...")
            
            // Primeiro, listar para encontrar o ID da curtida
            val listResponse = curtidaService.listarCurtida()
            Log.d("CURTIDA_API", "GET Curtidas Response: ${listResponse.code()}")
            
            if (listResponse.isSuccessful) {
                val curtidas = listResponse.body()?.curtidas ?: emptyList()
                Log.d("CURTIDA_API", "Total curtidas encontradas: ${curtidas.size}")
                
                val minhaCurtida = curtidas.find { 
                    it.idUser == userId && it.idPublicacao == postId 
                }
                
                if (minhaCurtida != null) {
                    Log.d("CURTIDA_API", "🎯 Curtida encontrada, ID: ${minhaCurtida.id}")
                    
                    val deleteResponse = curtidaService.excluirCurtida(minhaCurtida.id)
                    Log.d("CURTIDA_API", "DELETE Response: ${deleteResponse.code()}")
                    
                    if (deleteResponse.isSuccessful) {
                        Log.d("CURTIDA_API", "✅ Curtida removida com sucesso!")
                        true
                    } else {
                        Log.e("CURTIDA_API", "❌ Erro ao remover curtida: ${deleteResponse.errorBody()?.string()}")
                        false
                    }
                } else {
                    Log.w("CURTIDA_API", "⚠️ Curtida não encontrada para remoção")
                    false
                }
            } else {
                Log.e("CURTIDA_API", "❌ Erro ao listar curtidas: ${listResponse.errorBody()?.string()}")
                false
            }
        }
        
        if (result) {
            // Buscar contador atualizado da view
            Log.d("CURTIDA_API", "🔄 Buscando contador atualizado da view...")
            try {
                val feedService = RetrofitFactory.getFeedService()
                val feedResponse = feedService.getFeedPublicacoes()
                
                if (feedResponse.isSuccessful && feedResponse.body()?.status == true) {
                    val updatedPost = feedResponse.body()!!.feed.find { it.idPublicacao == postId }
                    val newCount = updatedPost?.curtidasCount ?: 0
                    
                    Log.d("CURTIDA_API", "✅ Novo contador do banco: $newCount")
                    newCount
                } else {
                    Log.w("CURTIDA_API", "⚠️ Erro ao buscar contador atualizado")
                    -1 // Indica sucesso na operação mas falha ao buscar contador
                }
            } catch (e: Exception) {
                Log.e("CURTIDA_API", "💥 Erro ao buscar contador: ${e.message}")
                -1
            }
        } else {
            -2 // Indica falha na operação
        }
    } catch (e: Exception) {
        Log.e("CURTIDA_API", "💥 Erro geral: ${e.message}", e)
        -2
    }
}

/**
 * Mapeia múltiplas publicações sem verificar curtidas inicialmente
 */
fun mapMultipleFeedPublicacoes(
    feedPublicacoes: List<FeedPublicacao>
): List<PostOptimized> {
    return feedPublicacoes.map { feedPublicacao ->
        PostOptimized(
            id = feedPublicacao.idPublicacao,
            userName = "@${feedPublicacao.nomeUsuario}",
            userProfileImageUrl = feedPublicacao.fotoPerfil,
            postImageUrl = feedPublicacao.imagem,
            gymName = feedPublicacao.localizacao,
            caption = feedPublicacao.descricao,
            curtidasCount = feedPublicacao.curtidasCount,
            comentariosCount = feedPublicacao.comentariosCount,
            isLiked = false // Será atualizado posteriormente
        )
    }
}

// =================================================================================
// 3. TELA PRINCIPAL OTIMIZADA
// =================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenOptimized(navController: NavController) {
    
    val posts = remember { mutableStateListOf<PostOptimized>() }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val isDarkTheme = isSystemInDarkTheme()
    val logoRes = if (isDarkTheme) R.drawable.logo_escuro else R.drawable.logo_claro
    var selectedItem by remember { mutableStateOf<Int>(0) }
    val items = listOf("Home", "Treinos", "Conquistas", "Perfil")
    var notificacoesCount by remember { mutableStateOf(0) }

    // Carregamento otimizado usando FeedService
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val feedService = RetrofitFactory.getFeedService()
                val response = feedService.getFeedPublicacoes()
                
                if (response.isSuccessful && response.body() != null) {
                    val feedResponse = response.body()!!
                    if (feedResponse.status) {
                        val mappedPosts = mapMultipleFeedPublicacoes(feedResponse.feed)
                        posts.clear()
                        posts.addAll(mappedPosts)
                        errorMessage = null
                        Log.d("HomeOptimized", "✅ Feed carregado: ${mappedPosts.size} publicações")
                        
                        // Verificar curtidas após carregar os posts
                        mappedPosts.forEach { post ->
                            launch {
                                val isLiked = checkIfUserLikedPost(post.id, context)
                                val index = posts.indexOfFirst { it.id == post.id }
                                if (index >= 0) {
                                    posts[index] = posts[index].copy(isLiked = isLiked)
                                }
                            }
                        }
                    } else {
                        errorMessage = "Erro ao carregar feed"
                    }
                } else {
                    errorMessage = "Erro na resposta: ${response.code()}"
                    Log.e("HomeOptimized", "Erro na API: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage = "Erro de conexão: ${e.message}"
                Log.e("HomeOptimized", "Erro de conexão", e)
            } finally {
                isLoading = false
            }
        }
    }

    // Carregar contagem de notificações
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val userId = UserPreferences.getUserId(context)
                val notificacaoService = RetrofitFactory.getNotificacaoService()
                val response = notificacaoService.contarNotificacoesNaoLidas(userId)
                
                if (response.isSuccessful && response.body() != null) {
                    notificacoesCount = response.body()!!["count"] ?: 0
                }
            } catch (e: Exception) {
                Log.e("HomeOptimized", "Erro ao carregar notificações", e)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = logoRes),
                            contentDescription = "Logo",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GymBuddy", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (notificacoesCount > 0) {
                                Badge { Text("$notificacoesCount") }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = { navController.navigate("notificacoes") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificações"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            val iconColor = if (selectedItem == index) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface
                            
                            when (item) {
                                "Home" -> Icon(
                                    imageVector = Icons.Filled.Home,
                                    contentDescription = item,
                                    modifier = Modifier.size(28.dp),
                                    tint = iconColor
                                )
                                "Treinos" -> Icon(
                                    imageVector = Icons.Filled.FitnessCenter,
                                    contentDescription = item,
                                    modifier = Modifier.size(28.dp),
                                    tint = iconColor
                                )
                                "Conquistas" -> Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = item,
                                    modifier = Modifier.size(28.dp),
                                    tint = iconColor
                                )
                                "Perfil" -> Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = item,
                                    modifier = Modifier.size(28.dp),
                                    tint = iconColor
                                )
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
                    contentDescription = "Adicionar publicação",
                    modifier = Modifier.size(28.dp)
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
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(
                            onClick = {
                                isLoading = true
                                coroutineScope.launch {
                                    try {
                                        val feedService = RetrofitFactory.getFeedService()
                                        val response = feedService.getFeedPublicacoes()
                                        
                                        if (response.isSuccessful && response.body() != null) {
                                            val feedResponse = response.body()!!
                                            if (feedResponse.status) {
                                                val mappedPosts = mapMultipleFeedPublicacoes(feedResponse.feed)
                                                posts.clear()
                                                posts.addAll(mappedPosts)
                                                errorMessage = null
                                                
                                                // Verificar curtidas após recarregar
                                                mappedPosts.forEach { post ->
                                                    launch {
                                                        val isLiked = checkIfUserLikedPost(post.id, context)
                                                        val index = posts.indexOfFirst { it.id == post.id }
                                                        if (index >= 0) {
                                                            posts[index] = posts[index].copy(isLiked = isLiked)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("HomeOptimized", "Erro ao tentar novamente", e)
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
                        items(posts) { post ->
                            PostItemOptimized(
                                post = post,
                                onLikeClick = { isLiked, onLikeResult ->
                                    Log.d("CURTIDA_API", "🏠 HOME: Clique no post ${post.id}, isLiked: $isLiked")
                                    
                                    coroutineScope.launch {
                                        val result = toggleLike(post.id, isLiked, context)
                                        Log.d("CURTIDA_API", "📊 Toggle result: $result")
                                        
                                        when {
                                            result >= 0 -> {
                                                // Sucesso - usar contador do banco
                                                Log.d("CURTIDA_API", "✅ Sucesso! Novo contador: $result")
                                                onLikeResult(true, result)
                                            }
                                            result == -1 -> {
                                                // Sucesso na operação, mas falha ao buscar contador
                                                Log.d("CURTIDA_API", "✅ Operação bem-sucedida, contador não atualizado")
                                                onLikeResult(true, -1)
                                            }
                                            else -> {
                                                // Falha na operação
                                                Log.e("CURTIDA_API", "❌ Falha na operação")
                                                onLikeResult(false, -1)
                                            }
                                        }
                                    }
                                },
                                onCommentClick = {
                                    navController.navigate("comentarios/${post.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// =================================================================================
// 4. COMPONENTE OTIMIZADO DO POST
// =================================================================================

@Composable
fun PostItemOptimized(
    post: PostOptimized,
    onLikeClick: (Boolean, (Boolean, Int) -> Unit) -> Unit,
    onCommentClick: () -> Unit
) {
    var isLiked by remember { mutableStateOf(post.isLiked) }
    var likesCount by remember { mutableStateOf(post.curtidasCount) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        // Header do post
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = post.userProfileImageUrl,
                contentDescription = "Foto de perfil",
                modifier = Modifier.size(40.dp).clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.profile_placeholder),
                error = painterResource(id = R.drawable.profile_placeholder)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(post.userName, fontWeight = FontWeight.Bold)
                if (post.gymName != null) {
                    Text(post.gymName, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        // Imagem do post
        AsyncImage(
            model = post.postImageUrl,
            contentDescription = "Imagem da publicação",
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.img),
            error = painterResource(id = R.drawable.img)
        )

        // Botões de interação
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)) {
            IconButton(onClick = {
                // TESTE BÁSICO - APENAS VERIFICAR SE BOTÃO FUNCIONA
                Log.e("BOTAO_TESTE", "🚨🚨🚨 BOTÃO FOI CLICADO! POST: ${post.id} 🚨🚨🚨")
                
                // Atualizar estado local imediatamente
                val novoEstado = !isLiked
                isLiked = novoEstado
                likesCount = if (novoEstado) likesCount + 1 else likesCount - 1
                
                Log.e("BOTAO_TESTE", "✅ UI ATUALIZADA: isLiked=$isLiked, count=$likesCount")
                
                // Chamar API
                onLikeClick(novoEstado) { success, newCount ->
                    Log.d("CURTIDA_API", "📡 CALLBACK: success=$success, newCount=$newCount")
                    
                    if (success) {
                        // Se teve sucesso, atualizar contador se disponível
                        if (newCount >= 0) {
                            likesCount = newCount
                            Log.d("CURTIDA_API", "✅ Contador atualizado: $newCount")
                        }
                        // Se newCount = -1, manter contador atual (operação sucesso mas sem contador)
                    } else {
                        // Se falhou, reverter
                        isLiked = !novoEstado
                        likesCount = if (!novoEstado) likesCount + 1 else likesCount - 1
                        Log.e("CURTIDA_API", "❌ REVERTIDO: isLiked=$isLiked, count=$likesCount")
                    }
                }
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
            IconButton(onClick = { /* Compartilhar */ }) {
                Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Compartilhar", modifier = Modifier.size(28.dp))
            }
        }

        // Contadores
        Text(
            text = "$likesCount curtidas",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        
        if (post.comentariosCount > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ver todos os ${post.comentariosCount} comentários",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
        
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
// 5. PREVIEWS
// =================================================================================

@Preview(name = "Light Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun HomeScreenOptimizedLightPreview() {
    MobileGYMBUDDYTheme(darkTheme = false) { 
        HomeScreenOptimized(navController = rememberNavController()) 
    }
}

@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenOptimizedDarkPreview() {
    MobileGYMBUDDYTheme(darkTheme = true) { 
        HomeScreenOptimized(navController = rememberNavController()) 
    }
}
