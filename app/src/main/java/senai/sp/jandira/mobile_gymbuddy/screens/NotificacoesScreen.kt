package senai.sp.jandira.mobile_gymbuddy.screens

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

/**
 * Tela de Notificações que consome a VIEW vw_notificacoes_detalhadas
 * As notificações são criadas automaticamente pelas TRIGGERS do banco
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacoesScreen(navController: NavController) {
    
    val notificacoes = remember { mutableStateListOf<Notificacao>() }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Carregar notificações usando a view otimizada
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val userId = UserPreferences.getUserId(context)
                val notificacaoService = RetrofitFactory.getNotificacaoService()
                val response = notificacaoService.getNotificacoesUsuario(userId)
                
                if (response.isSuccessful && response.body() != null) {
                    val notificacaoResponse = response.body()!!
                    if (notificacaoResponse.status) {
                        notificacoes.clear()
                        notificacoes.addAll(notificacaoResponse.notificacoes)
                        errorMessage = null
                        Log.d("NotificacoesScreen", "✅ ${notificacaoResponse.notificacoes.size} notificações carregadas")
                    } else {
                        errorMessage = "Erro ao carregar notificações"
                    }
                } else {
                    errorMessage = "Erro na resposta: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Erro de conexão: ${e.message}"
                Log.e("NotificacoesScreen", "Erro ao carregar notificações", e)
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notificações") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (notificacoes.any { !it.isLida }) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        val userId = UserPreferences.getUserId(context)
                                        val notificacaoService = RetrofitFactory.getNotificacaoService()
                                        notificacaoService.marcarTodasComoLidas(userId)
                                        
                                        // Atualizar UI localmente
                                        notificacoes.replaceAll { it.copy(isLida = true) }
                                        Log.d("NotificacoesScreen", "✅ Todas as notificações marcadas como lidas")
                                    } catch (e: Exception) {
                                        Log.e("NotificacoesScreen", "Erro ao marcar como lidas", e)
                                    }
                                }
                            }
                        ) {
                            Text("Marcar todas como lidas")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
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
                notificacoes.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = "Sem notificações",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Você não tem notificações",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Quando alguém curtir ou comentar em suas publicações, você verá aqui!",
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
                        items(notificacoes) { notificacao ->
                            NotificacaoItem(
                                notificacao = notificacao,
                                onClick = {
                                    coroutineScope.launch {
                                        // Marcar como lida ao clicar
                                        if (!notificacao.isLida) {
                                            try {
                                                val notificacaoService = RetrofitFactory.getNotificacaoService()
                                                notificacaoService.marcarComoLida(notificacao.id)
                                                
                                                // Atualizar UI
                                                val index = notificacoes.indexOf(notificacao)
                                                if (index != -1) {
                                                    notificacoes[index] = notificacao.copy(isLida = true)
                                                }
                                            } catch (e: Exception) {
                                                Log.e("NotificacoesScreen", "Erro ao marcar como lida", e)
                                            }
                                        }
                                        
                                        // Navegar para a publicação se disponível
                                        notificacao.idPublicacao?.let { publicacaoId ->
                                            navController.navigate("publicacao/$publicacaoId")
                                        }
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

@Composable
fun NotificacaoItem(
    notificacao: Notificacao,
    onClick: () -> Unit
) {
    val backgroundColor = if (!notificacao.isLida) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
    } else {
        Color.Transparent
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (!notificacao.isLida) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícone baseado no tipo de notificação
            val iconInfo = when (notificacao.tipoNotificacao) {
                TipoNotificacao.CURTIDA_PUBLICACAO.valor -> {
                    Icons.Default.Favorite to MaterialTheme.colorScheme.secondary
                }
                TipoNotificacao.COMENTARIO.valor -> {
                    Icons.Default.ChatBubble to MaterialTheme.colorScheme.primary
                }
                TipoNotificacao.CURTIDA_COMENTARIO.valor -> {
                    Icons.Default.ThumbUp to MaterialTheme.colorScheme.tertiary
                }
                else -> Icons.Default.Notifications to MaterialTheme.colorScheme.onSurface
            }
            
            Icon(
                imageVector = iconInfo.first,
                contentDescription = notificacao.tipoNotificacao,
                tint = iconInfo.second,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notificacao.textoNotificacao,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (!notificacao.isLida) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatarTempo(notificacao.dataCriacao),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!notificacao.isLida) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                )
            }
        }
    }
}

/**
 * Formata a data da notificação para exibição amigável
 */
fun formatarTempo(dataString: String): String {
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

@Preview(name = "Light Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun NotificacoesScreenPreview() {
    MobileGYMBUDDYTheme(darkTheme = false) {
        NotificacoesScreen(navController = rememberNavController())
    }
}

@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NotificacoesScreenDarkPreview() {
    MobileGYMBUDDYTheme(darkTheme = true) {
        NotificacoesScreen(navController = rememberNavController())
    }
}
