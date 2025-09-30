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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import senai.sp.jandira.mobile_gymbuddy.R
import senai.sp.jandira.mobile_gymbuddy.ui.theme.MobileGYMBUDDYTheme

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
// 3. A TELA PRINCIPAL
// =================================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val posts = remember { mutableStateListOf(*mockApiData.toTypedArray()) }
    val isDarkTheme = isSystemInDarkTheme()
    val logoRes = if (isDarkTheme) R.drawable.logo_escuro else R.drawable.logo_claro
    var selectedItem by remember { mutableStateOf<Int>(0) }
    val items = listOf("Home", "Treinos", "Conquistas", "Perfil")
    val sheetState = rememberModalBottomSheetState()
    var selectedPostForComments by remember { mutableStateOf<Post?>(null) }


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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
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
// 4. COMPONENTE DO POST
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
// 5. CONTEÚDO DA BOTTOM SHEET DE COMENTÁRIOS
// =================================================================================
@Composable
fun CommentsSheetContent(
    post: Post,
    onAddComment: (String) -> Unit
) {
    var newCommentText by remember { mutableStateOf("") }
    val isSendEnabled = newCommentText.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        Text("Comentários", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(post.comments) { comment ->
                CommentItem(comment = comment)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ***** AQUI ESTÁ A PARTE DE ADICIONAR COMENTÁRIOS *****
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newCommentText,
                onValueChange = { newCommentText = it },
                label = { Text("Adicione um comentário...") },
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    onAddComment(newCommentText)
                    newCommentText = ""
                },
                enabled = isSendEnabled
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar comentário",
                    tint = if (isSendEnabled) MaterialTheme.colorScheme.secondary else Color.Gray
                )
            }
        }
    }
}

// =================================================================================
// 6. ITEM INDIVIDUAL DE COMENTÁRIO
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
// 7. PREVIEWS
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