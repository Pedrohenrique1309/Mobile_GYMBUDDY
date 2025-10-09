package senai.sp.jandira.mobile_gymbuddy.screens

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import java.text.SimpleDateFormat
import java.util.*

// =================================================================================
// 1. MODELO DE DADOS OTIMIZADO PARA UI
// =================================================================================

data class ComentarioUI(
    val id: Int,
    val userName: String,
    val userProfileImageUrl: String?,
    val texto: String,
    val curtidasCount: Int,
    val isLiked: Boolean = false,
    val dataComentario: String
)

// =================================================================================
// 2. FUNÇÃO DE MAPEAMENTO OTIMIZADA
// =================================================================================

fun mapComentarioViewToUI(comentarioView: ComentarioView): ComentarioUI {
    return ComentarioUI(
        id = comentarioView.idComentarios,
        userName = "@${comentarioView.nomeUsuario}",
        userProfileImageUrl = comentarioView.fotoPerfil,
        texto = comentarioView.conteudoComentario,
        curtidasCount = comentarioView.curtidasCount,
        dataComentario = comentarioView.dataComentario
    )
}

// =================================================================================
// 3. TELA PRINCIPAL DE COMENTÁRIOS OTIMIZADA
// =================================================================================

/**
 * Tela de comentários otimizada que usa a VIEW vw_comentarios_publicacao
 * Os comentários são inseridos aproveitando as TRIGGERS automáticas do banco
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComentariosScreenOptimized(
    navController: NavController,
    publicacaoId: Int
) {
    val comentarios = remember { mutableStateListOf<ComentarioUI>() }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var novoComentarioTexto by remember { mutableStateOf("") }
    var isEnviandoComentario by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Carregar comentários usando a view otimizada
    LaunchedEffect(publicacaoId) {
        coroutineScope.launch {
            try {
                val comentarioService = RetrofitFactory.getComentarioService()
                val response = comentarioService.getComentarios(publicacaoId)
                
                if (response.isSuccessful && response.body() != null) {
                    val comentarioResponse = response.body()!!
                    if (comentarioResponse.status) {
                        val comentariosUI = comentarioResponse.comentarios.map { comentarioApi ->
                            ComentarioUI(
                                id = comentarioApi.id,
                                userName = "@${comentarioApi.user.firstOrNull()?.nickname ?: "user"}",
                                userProfileImageUrl = comentarioApi.user.firstOrNull()?.foto,
                                texto = comentarioApi.conteudo,
                                curtidasCount = 0, // Será implementado depois
                                dataComentario = comentarioApi.dataComentario
                            )
                        }
                        comentarios.clear()
                        comentarios.addAll(comentariosUI)
                        errorMessage = null
                        Log.d("ComentariosOptimized", "✅ ${comentariosUI.size} comentários carregados")
                    } else {
                        errorMessage = "Erro ao carregar comentários"
                    }
                } else {
                    errorMessage = "Erro na resposta: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Erro de conexão: ${e.message}"
                Log.e("ComentariosOptimized", "Erro ao carregar comentários", e)
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Comentários") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Barra de adicionar comentário
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = novoComentarioTexto,
                        onValueChange = { novoComentarioTexto = it },
                        label = { Text("Adicione um comentário...") },
                        modifier = Modifier.weight(1f),
                        enabled = !isEnviandoComentario,
                        maxLines = 3
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            if (novoComentarioTexto.isNotBlank()) {
                                isEnviandoComentario = true
                                coroutineScope.launch {
                                    try {
                                        val comentarioService = RetrofitFactory.getComentarioService()
                                        val userId = UserPreferences.getUserId(context)
                                        val userNickname = UserPreferences.getUserNickname(context)
                                        val userPhotoUrl = UserPreferences.getUserPhotoUrl(context)
                                        
                                        val novoComentario = ComentarioRequest(
                                            conteudo = novoComentarioTexto,
                                            dataComentario = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                                            idPublicacao = publicacaoId,
                                            idUser = userId
                                        )
                                        
                                        val response = comentarioService.criarComentario(novoComentario)
                                        
                                        if (response.isSuccessful) {
                                            // Adicionar comentário localmente
                                            // As TRIGGERS do banco já atualizaram o contador automaticamente
                                            val comentarioUI = ComentarioUI(
                                                id = (0..10000).random(),
                                                userName = "@$userNickname",
                                                userProfileImageUrl = userPhotoUrl,
                                                texto = novoComentarioTexto,
                                                curtidasCount = 0,
                                                dataComentario = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                                            )
                                            
                                            comentarios.add(0, comentarioUI)
                                            novoComentarioTexto = ""
                                            
                                            Log.d("ComentariosOptimized", "✅ Comentário adicionado com triggers automáticas")
                                        } else {
                                            Log.e("ComentariosOptimized", "❌ Erro ao adicionar comentário: ${response.code()}")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("ComentariosOptimized", "Erro ao enviar comentário", e)
                                    } finally {
                                        isEnviandoComentario = false
                                    }
                                }
                            }
                        },
                        enabled = novoComentarioTexto.isNotBlank() && !isEnviandoComentario
                    ) {
                        if (isEnviandoComentario) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Enviar comentário",
                                tint = if (novoComentarioTexto.isNotBlank()) 
                                    MaterialTheme.colorScheme.secondary 
                                else 
                                    Color.Gray
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                errorMessage != null -> {
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
                    }
                }
                comentarios.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Sem comentários",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhum comentário ainda",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Seja o primeiro a comentar!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(comentarios) { comentario ->
                            ComentarioItemOptimized(
                                comentario = comentario,
                                onLikeClick = { isLiked ->
                                    coroutineScope.launch {
                                        toggleComentarioLike(comentario.id, isLiked, context)
                                    }
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
// 4. COMPONENTE OTIMIZADO DO COMENTÁRIO
// =================================================================================

@Composable
fun ComentarioItemOptimized(
    comentario: ComentarioUI,
    onLikeClick: (Boolean) -> Unit
) {
    var isLiked by remember { mutableStateOf(comentario.isLiked) }
    var likesCount by remember { mutableStateOf(comentario.curtidasCount) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = comentario.userProfileImageUrl,
            contentDescription = "Foto de ${comentario.userName}",
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
            placeholder = painterResource(id = R.drawable.profile_placeholder),
            error = painterResource(id = R.drawable.profile_placeholder),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(comentario.userName)
                    }
                    append(" ")
                    append(comentario.texto)
                },
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatarTempoComentario(comentario.dataComentario),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (likesCount > 0) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "$likesCount curtidas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = {
                    isLiked = !isLiked
                    if (isLiked) likesCount++ else likesCount--
                    onLikeClick(isLiked)
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Curtir comentário",
                    tint = if (isLiked) MaterialTheme.colorScheme.secondary else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// =================================================================================
// 5. FUNÇÃO OTIMIZADA DE CURTIDA EM COMENTÁRIOS
// =================================================================================

/**
 * Função que usa as procedures do banco para curtidas em comentários
 */
suspend fun toggleComentarioLike(comentarioId: Int, isLiked: Boolean, context: Context) {
    try {
        val userId = UserPreferences.getUserId(context)
        val curtidaService = RetrofitFactory.getCurtidaService()
        
        val request = CurtidaComentarioRequest(
            idComentario = comentarioId,
            idUser = userId
        )
        
        val response = if (isLiked) {
            curtidaService.adicionarCurtidaComentario(request)
        } else {
            curtidaService.removerCurtidaComentario(request)
        }
        
        if (response.isSuccessful) {
            Log.d("ToggleComentarioLike", "✅ Curtida ${if (isLiked) "adicionada" else "removida"} com procedures")
        } else {
            Log.e("ToggleComentarioLike", "❌ Erro: ${response.code()}")
        }
    } catch (e: Exception) {
        Log.e("ToggleComentarioLike", "Erro ao alterar curtida", e)
    }
}

// =================================================================================
// 6. FUNÇÃO AUXILIAR
// =================================================================================

fun formatarTempoComentario(dataString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val data = inputFormat.parse(dataString)
        val agora = Date()
        
        val diffInMillis = agora.time - (data?.time ?: 0)
        val diffInMinutos = diffInMillis / (1000 * 60)
        val diffInHoras = diffInMinutos / 60
        val diffInDias = diffInHoras / 24
        
        when {
            diffInMinutos < 1 -> "Agora"
            diffInMinutos < 60 -> "${diffInMinutos}m"
            diffInHoras < 24 -> "${diffInHoras}h"
            diffInDias < 7 -> "${diffInDias}d"
            else -> {
                val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                outputFormat.format(data)
            }
        }
    } catch (e: Exception) {
        "Recente"
    }
}

// =================================================================================
// 7. PREVIEWS
// =================================================================================

@Preview(name = "Light Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun ComentariosScreenOptimizedPreview() {
    MobileGYMBUDDYTheme(darkTheme = false) {
        ComentariosScreenOptimized(navController = rememberNavController(), publicacaoId = 1)
    }
}

@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ComentariosScreenOptimizedDarkPreview() {
    MobileGYMBUDDYTheme(darkTheme = true) {
        ComentariosScreenOptimized(navController = rememberNavController(), publicacaoId = 1)
    }
}
