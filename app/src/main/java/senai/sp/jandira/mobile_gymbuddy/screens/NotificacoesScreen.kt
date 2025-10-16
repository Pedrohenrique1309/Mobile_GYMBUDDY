package senai.sp.jandira.mobile_gymbuddy.screens

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import senai.sp.jandira.mobile_gymbuddy.data.model.Notificacao
import senai.sp.jandira.mobile_gymbuddy.data.model.TipoNotificacao
import senai.sp.jandira.mobile_gymbuddy.data.repository.NotificacaoRepository
import senai.sp.jandira.mobile_gymbuddy.ui.theme.MobileGYMBUDDYTheme
import senai.sp.jandira.mobile_gymbuddy.utils.UserPreferences

// =================================================================================
// 1. MODELOS DE DADOS PARA NOTIFICAÇÕES
// =================================================================================

data class NotificationItem(
    val id: Int,
    val type: NotificationType,
    val userName: String,
    val postThumbnail: String? = null,
    val commentText: String? = null
)

enum class NotificationType {
    LIKE, FOLLOW, COMMENT
}

// =================================================================================
// 2. COMPONENTE PARA NOTIFICAÇÕES DA API
// =================================================================================

/**
 * Componente para renderizar notificações vindas da API
 */
@Composable
fun NotificationCardFromApi(
    notificacao: Notificacao,
    onNotificationClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onNotificationClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (notificacao.isLida) Color.Gray else Color.Red),
        colors = CardDefaults.cardColors(
            containerColor = if (notificacao.isLida) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar do usuário
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Texto da notificação
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notificacao.textoNotificacao,
                    fontSize = 14.sp,
                    color = if (notificacao.isLida) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (notificacao.isLida) FontWeight.Normal else FontWeight.Medium
                )
                
                Text(
                    text = formatarDataNotificacao(notificacao.dataCriacao),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Indicador de não lida
            if (!notificacao.isLida) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

/**
 * Função para formatar a data da notificação
 */
fun formatarDataNotificacao(dataString: String): String {
    return try {
        // Assumindo que a data vem no formato "yyyy-MM-dd HH:mm:ss"
        // Você pode ajustar conforme o formato real da sua API
        val partes = dataString.split(" ")
        if (partes.size >= 2) {
            val data = partes[0]
            val hora = partes[1].substring(0, 5) // HH:mm
            "$data às $hora"
        } else {
            dataString
        }
    } catch (e: Exception) {
        dataString
    }
}

/**
 * Tela de Notificações seguindo exatamente o design da imagem fornecida
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacoesScreen(navController: NavController) {
    val darkTheme = isSystemInDarkTheme()
    val logoRes = if (darkTheme) R.drawable.logo_escuro else R.drawable.logo_claro
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Estados para as notificações
    var notificacoes by remember { mutableStateOf<List<Notificacao>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Repository para buscar notificações
    val notificacaoRepository = remember { NotificacaoRepository() }
    
    // Buscar notificações ao carregar a tela
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val userId = UserPreferences.getUserId(context)
                // Usar a view de notificações detalhada
                notificacaoRepository.getTodasNotificacoes().collect { notifs ->
                    // Filtrar notificações para o usuário logado
                    val notificacoesDoUsuario = notifs.filter { it.idUsuarioDestino == userId }
                    notificacoes = notificacoesDoUsuario
                    isLoading = false
                }
            } catch (e: Exception) {
                errorMessage = "Erro ao carregar notificações: ${e.message}"
                isLoading = false
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header com logo e ícones
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Seta voltar
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Logo GYM BUDDY
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = "Logo GYM BUDDY",
                modifier = Modifier.height(80.dp)
            )
            
            // Espaço vazio para manter o layout balanceado
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        // Lista de notificações
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Erro desconhecido",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            notificacoes.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma notificação encontrada",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notificacoes) { notificacao ->
                        NotificationCardFromApi(
                            notificacao = notificacao,
                            onNotificationClick = { 
                                // Marcar como lida e navegar se necessário
                                coroutineScope.launch {
                                    notificacaoRepository.marcarComoLida(notificacao.id)
                                }
                            }
                        )
                    }
                }
            }
        }
        
        // NavigationBar inferior - Mesmo da HomeScreen
        NavigationBar {
            val items = listOf("Home", "Treinos", "Conquistas", "Perfil")
            val selectedItem = 0 // Home é o item selecionado (vindo das notificações)
            
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
                                tint = iconColor
                            )
                            "Treinos" -> Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = item,
                                modifier = Modifier.size(28.dp),
                                tint = iconColor
                            )
                            "Conquistas" -> Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = item,
                                modifier = Modifier.size(28.dp),
                                tint = iconColor
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
                                        tint = iconColor
                                    )
                                }
                            }
                        }
                    },
                    selected = selectedItem == index,
                    onClick = { 
                        when (item) {
                            "Home" -> navController.navigate("home")
                            // Outras navegações podem ser adicionadas aqui
                        }
                    }
                )
            }
        }
    }
}

// =================================================================================
// 3. COMPONENTES ESPECÍFICOS PARA CADA TIPO DE NOTIFICAÇÃO (MANTIDOS PARA PREVIEW)
// =================================================================================/

/**
 * Componente para notificações de curtida - "curtiu sua publicação"
 */
@Composable
fun LikeNotificationCard(notification: NotificationItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Red),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar do usuário
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Texto da notificação
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                            append(notification.userName)
                        }
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                            append(" curtiu sua publicação")
                        }
                    },
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Miniatura da publicação
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

/**
 * Componente para notificações de seguidor - "começou a seguir você"
 */
@Composable
fun FollowNotificationCard(notification: NotificationItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Red),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar do usuário
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Texto da notificação
            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                        append(notification.userName)
                    }
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                        append(" começou a seguir você.")
                    }
                },
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Componente para notificações de comentário - "comentou na sua publicação"
 */
@Composable
fun CommentNotificationCard(notification: NotificationItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Red),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar do usuário
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Texto da notificação
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                            append(notification.userName)
                        }
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                            append(" comentou")
                        }
                    },
                    fontSize = 14.sp
                )
                
                notification.commentText?.let { comment ->
                    Text(
                        text = "\"$comment\"",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Miniatura da publicação
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

// =================================================================================
// 3. PREVIEWS
// =================================================================================

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

@Preview(name = "Like Notification Light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun LikeNotificationLightPreview() {
    MobileGYMBUDDYTheme(darkTheme = false) {
        LikeNotificationCard(
            NotificationItem(1, NotificationType.LIKE, "mailton_jose", "https://example.com/post1.jpg")
        )
    }
}

@Preview(name = "Like Notification Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LikeNotificationDarkPreview() {
    MobileGYMBUDDYTheme(darkTheme = true) {
        LikeNotificationCard(
            NotificationItem(1, NotificationType.LIKE, "mailton_jose", "https://example.com/post1.jpg")
        )
    }
}

@Preview(name = "Follow Notification Light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun FollowNotificationLightPreview() {
    MobileGYMBUDDYTheme(darkTheme = false) {
        FollowNotificationCard(
            NotificationItem(2, NotificationType.FOLLOW, "mailton_jose")
        )
    }
}

@Preview(name = "Follow Notification Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FollowNotificationDarkPreview() {
    MobileGYMBUDDYTheme(darkTheme = true) {
        FollowNotificationCard(
            NotificationItem(2, NotificationType.FOLLOW, "mailton_jose")
        )
    }
}

@Preview(name = "Comment Notification Light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun CommentNotificationLightPreview() {
    MobileGYMBUDDYTheme(darkTheme = false) {
        CommentNotificationCard(
            NotificationItem(3, NotificationType.COMMENT, "mailton_jose", "https://example.com/post2.jpg", "parabéns, continue assim que você alc...")
        )
    }
}

@Preview(name = "Comment Notification Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CommentNotificationDarkPreview() {
    MobileGYMBUDDYTheme(darkTheme = true) {
        CommentNotificationCard(
            NotificationItem(3, NotificationType.COMMENT, "mailton_jose", "https://example.com/post2.jpg", "parabéns, continue assim que você alc...")
        )
    }
}
