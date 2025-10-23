package senai.sp.jandira.mobile_gymbuddy.screens

package senai.sp.jandira.mobile_gymbuddy.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import senai.sp.jandira.mobile_gymbuddy.R
import senai.sp.jandira.mobile_gymbuddy.data.model.IACategory
import senai.sp.jandira.mobile_gymbuddy.data.model.IACategoryConstants
import senai.sp.jandira.mobile_gymbuddy.ui.theme.MobileGYMBUDDYTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IAScreen(navController: NavController) {
    val isDarkTheme = isSystemInDarkTheme()
    val logoRes = if (isDarkTheme) R.drawable.logo_escuro else R.drawable.logo_claro

    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Lista de mensagens para simulação da conversa
    val messages = remember { mutableStateListOf<ChatMessage>() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = "Logo Gym Buddy",
                        modifier = Modifier.height(75.dp)
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("notifications") }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notificações",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val items = listOf("Home", "Treinos", "Conquistas", "Perfil")
                val selectedItem = 2 // IA como "Conquistas"

                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
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
                                "Perfil" -> Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = item,
                                    modifier = Modifier.size(28.dp),
                                    tint = iconColor
                                )
                            }
                        },
                        selected = selectedItem == index,
                        onClick = {
                            when (index) {
                                0 -> navController.navigate("home")
                                1 -> { /* Treinos */ }
                                2 -> { /* Já está na IA */ }
                                3 -> navController.navigate("editProfile")
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Se não há mensagens, mostra a tela inicial
            if (messages.isEmpty()) {
                InitialIAScreen(
                    onCategorySelected = { category ->
                        selectedCategory = category
                    },
                    onMessageSent = { message, category ->
                        // Simular envio de mensagem
                        messages.add(ChatMessage(message, true, category))
                        // Simular resposta da IA
                        messages.add(ChatMessage(getSimulatedResponse(category), false, category))
                    }
                )
            } else {
                // Mostra o chat
                ChatScreen(
                    messages = messages,
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    onSendMessage = { message ->
                        if (message.isNotBlank()) {
                            messages.add(ChatMessage(message, true, selectedCategory ?: "geral"))
                            // Simular resposta da IA
                            messages.add(ChatMessage(getSimulatedResponse(selectedCategory ?: "geral"), false, selectedCategory ?: "geral"))
                            inputText = ""
                        }
                    },
                    isLoading = isLoading
                )
            }
        }
    }
}

@Composable
fun InitialIAScreen(
    onCategorySelected: (String) -> Unit,
    onMessageSent: (String, String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Título principal
        Text(
            text = "Como posso ajudar você hoje?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Grade de categorias
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(IACategoryConstants.categories) { category ->
                CategoryCard(
                    category = category,
                    onClick = {
                        onCategorySelected(category.id)
                        // Simular mensagem automática baseada na categoria
                        val autoMessage = when (category.id) {
                            "hipertrofia" -> "Me ajude com dicas de hipertrofia"
                            "treinos" -> "Preciso de um treino personalizado"
                            "dieta" -> "Me oriente sobre nutrição"
                            "mais" -> "Tenho algumas dúvidas sobre fitness"
                            else -> "Como você pode me ajudar?"
                        }
                        onMessageSent(autoMessage, category.id)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Campo de input na parte inferior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        "Vamos treinar hoje?",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onMessageSent(inputText, "geral")
                        inputText = ""
                    }
                },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Enviar",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: IACategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ícone baseado na categoria
            val icon = when (category.icon) {
                "muscle" -> Icons.Default.FitnessCenter
                "fitness" -> Icons.Default.DirectionsRun
                "nutrition" -> Icons.Default.Restaurant
                "more" -> Icons.Default.MoreHoriz
                else -> Icons.Default.Help
            }

            Icon(
                imageVector = icon,
                contentDescription = category.title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = category.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Área de mensagens
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message = message)
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("IA está digitando...")
                            }
                        }
                    }
                }
            }
        }

        // Campo de input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                placeholder = { Text("Digite sua mensagem...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                onClick = { onSendMessage(inputText) },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Enviar",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = if (message.isUser)
                    MaterialTheme.colorScheme.onSecondary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Modelo de dados para mensagens do chat
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val category: String
)

// Função para simular respostas da IA
fun getSimulatedResponse(category: String): String {
    return when (category) {
        "hipertrofia" -> "Para hipertrofia muscular, é importante focar em exercícios com sobrecarga progressiva, realizar de 6-12 repetições por série, e manter uma frequência de treino adequada. Não esqueça da importância do descanso e da alimentação rica em proteínas!"
        "treinos" -> "Posso te ajudar a criar um treino personalizado! Que tipo de treino você prefere? Musculação, funcional, cardio? E quantos dias por semana você pode treinar?"
        "dieta" -> "Uma boa nutrição é fundamental para seus resultados! Para ganho de massa muscular, recomendo consumir cerca de 1,6-2,2g de proteína por kg de peso corporal, manter carboidratos para energia e não esquecer das gorduras boas."
        "mais" -> "Estou aqui para te ajudar com qualquer dúvida sobre fitness! Posso orientar sobre suplementação, técnicas de exercícios, recuperação muscular, motivação para treinar e muito mais. O que você gostaria de saber?"
        else -> "Olá! Sou seu assistente de fitness virtual. Posso te ajudar com treinos, nutrição, hipertrofia e muito mais. Como posso te ajudar hoje?"
    }
}

@Preview(showBackground = true)
@Composable
fun IAScreenPreview() {
    MobileGYMBUDDYTheme {
        IAScreen(navController = rememberNavController())
    }
}
