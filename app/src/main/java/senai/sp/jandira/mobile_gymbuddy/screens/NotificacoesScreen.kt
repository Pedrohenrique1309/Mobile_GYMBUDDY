package senai.sp.jandira.mobile_gymbuddy.screens

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import senai.sp.jandira.mobile_gymbuddy.R
import senai.sp.jandira.mobile_gymbuddy.ui.theme.MobileGYMBUDDYTheme

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

// Dados mockados conforme a imagem
val mockNotifications = listOf(
    NotificationItem(1, NotificationType.LIKE, "mailton_jose", "https://example.com/post1.jpg"),
    NotificationItem(2, NotificationType.FOLLOW, "mailton_jose"),
    NotificationItem(3, NotificationType.COMMENT, "mailton_jose", "https://example.com/post2.jpg", "parabéns, continue assim que você alc..."),
    NotificationItem(4, NotificationType.LIKE, "mailton_jose", "https://example.com/post3.jpg"),
    NotificationItem(5, NotificationType.FOLLOW, "mailton_jose"),
    NotificationItem(6, NotificationType.COMMENT, "mailton_jose", "https://example.com/post4.jpg", "parabéns, continue assim que você alc..."),
    NotificationItem(7, NotificationType.LIKE, "mailton_jose", "https://example.com/post5.jpg"),
    NotificationItem(8, NotificationType.FOLLOW, "mailton_jose"),
    NotificationItem(9, NotificationType.COMMENT, "mailton_jose", "https://example.com/post6.jpg", "parabéns, continue assim que você alc...")
)

/**
 * Tela de Notificações seguindo exatamente o design da imagem fornecida
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacoesScreen(navController: NavController) {
    val darkTheme = isSystemInDarkTheme()
    val logoRes = if (darkTheme) R.drawable.logo_escuro else R.drawable.logo_claro
    
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
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Logo GYM BUDDY
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = "Logo GYM BUDDY",
                modifier = Modifier.height(40.dp)
            )
            
            // Ícone de notificações com badge
            Box {
                IconButton(onClick = { navController.navigate("home") }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notificações",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Badge "99+"
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .offset(x = 12.dp, y = (-4).dp)
                        .background(
                            MaterialTheme.colorScheme.error,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "99+",
                        color = MaterialTheme.colorScheme.onError,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Lista de notificações
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mockNotifications) { notification ->
                when (notification.type) {
                    NotificationType.LIKE -> LikeNotificationCard(notification)
                    NotificationType.FOLLOW -> FollowNotificationCard(notification)
                    NotificationType.COMMENT -> CommentNotificationCard(notification)
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
// 2. COMPONENTES ESPECÍFICOS PARA CADA TIPO DE NOTIFICAÇÃO
// =================================================================================

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
