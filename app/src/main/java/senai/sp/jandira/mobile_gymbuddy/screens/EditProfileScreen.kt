package senai.sp.jandira.mobile_gymbuddy.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import senai.sp.jandira.mobile_gymbuddy.R
import senai.sp.jandira.mobile_gymbuddy.ui.theme.*
import senai.sp.jandira.mobile_gymbuddy.data.service.RetrofitFactory
import senai.sp.jandira.mobile_gymbuddy.data.model.UsuarioDetalhes
import senai.sp.jandira.mobile_gymbuddy.data.model.Publicacao
import senai.sp.jandira.mobile_gymbuddy.utils.UserPreferences
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController
) {
    val isDarkTheme = isSystemInDarkTheme()
    val logoRes = if (isDarkTheme) R.drawable.logo_escuro else R.drawable.logo_claro
    var selectedItem by remember { mutableStateOf<Int>(3) } // Perfil tab selected
    val items = listOf(stringResource(R.string.nav_home), stringResource(R.string.nav_workouts), stringResource(R.string.nav_achievements), stringResource(R.string.nav_profile))
    
    // Estados para carregar dados do usuário
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var usuarioDetalhes by remember { mutableStateOf<UsuarioDetalhes?>(null) }
    var userPublicacoes by remember { mutableStateOf<List<Publicacao>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Carregar dados do usuário
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val userId = UserPreferences.getUserId(context)
                val usuarioService = RetrofitFactory.getUsuarioService()
                
                android.util.Log.d("EditProfileScreen", "Carregando dados do usuário: $userId")
                
                val userResponse = usuarioService.buscarUsuarioPorId(userId)
                
                if (userResponse.isSuccessful && userResponse.body() != null) {
                    val apiResponse = userResponse.body()!!
                    if (apiResponse.status && apiResponse.usuario.isNotEmpty()) {
                        usuarioDetalhes = apiResponse.usuario[0]
                        android.util.Log.d("EditProfileScreen", "✅ Dados do usuário carregados: ${usuarioDetalhes?.nome}")
                        
                        // Carregar publicações do usuário
                        val publicacaoService = RetrofitFactory.getPublicacaoService()
                        val publicacoesResponse = publicacaoService.getPublicacoes()
                        
                        if (publicacoesResponse.isSuccessful && publicacoesResponse.body() != null) {
                            val publicacoesApiResponse = publicacoesResponse.body()!!
                            if (publicacoesApiResponse.status) {
                                // Filtrar apenas publicações do usuário logado
                                val publicacoesDoUsuario = publicacoesApiResponse.publicacoes.filter { publicacao: Publicacao ->
                                    publicacao.idUser == userId
                                }
                                userPublicacoes = publicacoesDoUsuario
                                android.util.Log.d("EditProfileScreen", "✅ ${publicacoesDoUsuario.size} publicações carregadas")
                            }
                        } else {
                            android.util.Log.w("EditProfileScreen", "⚠️ Erro ao carregar publicações: ${publicacoesResponse.code()}")
                        }
                    } else {
                        errorMessage = context.getString(R.string.user_not_found)
                        android.util.Log.e("EditProfileScreen", "❌ Usuário não encontrado na resposta")
                    }
                } else {
                    errorMessage = context.getString(R.string.profile_load_error, userResponse.code())
                    android.util.Log.e("EditProfileScreen", "❌ Erro na API: ${userResponse.code()}")
                }
            } catch (e: Exception) {
                errorMessage = context.getString(R.string.connection_error, e.message ?: "")
                android.util.Log.e("EditProfileScreen", "❌ Exceção: ${e.message}", e)
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
                        contentDescription = stringResource(R.string.logo_gym_buddy_desc),
                        modifier = Modifier
                            .height(72.dp)
                            .wrapContentWidth(),
                        contentScale = ContentScale.Fit
                    )
                },
                actions = {
                    BadgedBox(
                        badge = {
                            Badge { Text(stringResource(R.string.notifications_badge_99)) }
                        }
                    ) {
                        IconButton(
                            onClick = { navController.navigate("notifications") }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = stringResource(R.string.notifications_description),
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
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
                                stringResource(R.string.nav_home) -> Icon(
                                    imageVector = Icons.Filled.Home,
                                    contentDescription = item,
                                    modifier = Modifier.size(28.dp),
                                    tint = iconColor
                                )
                                stringResource(R.string.nav_workouts) -> Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = item,
                                    modifier = Modifier.size(28.dp),
                                    tint = iconColor
                                )
                                stringResource(R.string.nav_achievements) -> Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = item,
                                    modifier = Modifier.size(28.dp),
                                    tint = iconColor
                                )
                                stringResource(R.string.nav_profile) -> {
                                    BadgedBox(
                                        badge = {
                                            Badge { Text(stringResource(R.string.profile_badge_3)) }
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
                            selectedItem = index
                            when (index) {
                                0 -> navController.navigate("home")
                                1 -> { /* Treinos */ }
                                2 -> { /* Conquistas */ }
                                3 -> { /* Já está na tela de perfil */ }
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
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    // Loading State
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.loading_profile),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                } else if (errorMessage != null) {
                    // Error State
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = stringResource(R.string.error_icon),
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage!!,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                } else if (usuarioDetalhes != null) {
                    // Success State - Dados reais do usuário
                    
                    // Profile Picture
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        // Se o usuário tem foto, carregar com AsyncImage, senão mostrar ícone
                        if (!usuarioDetalhes!!.foto.isNullOrEmpty()) {
                            // TODO: Implementar AsyncImage quando tiver fotos reais
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(R.string.profile_photo),
                                modifier = Modifier.size(60.dp),
                                tint = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(R.string.profile_photo),
                                modifier = Modifier.size(60.dp),
                                tint = Color.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // User Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.username_label),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = usuarioDetalhes!!.nome,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Nickname
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.nickname_label),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "@${usuarioDetalhes!!.nickname}",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Description
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.description_label_profile),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = usuarioDetalhes!!.descricao ?: stringResource(R.string.no_description),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    // IMC (se disponível)
                    if (usuarioDetalhes!!.imc != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.imc_label),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = String.format("%.1f", usuarioDetalhes!!.imc),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Edit Profile Button
                    Button(
                        onClick = { /* TODO: Implementar edição de perfil */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.edit_profile_button),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // New Post Button
                    Button(
                        onClick = { navController.navigate("publishing") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.new_post_button),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Posts Grid - Dados reais das publicações
            if (!isLoading && errorMessage == null) {
                if (userPublicacoes.isNotEmpty()) {
                    // Mostrar grade de publicações
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(userPublicacoes) { publicacao ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .background(
                                        color = Color.LightGray,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            ) {
                                // Carregar imagem real da publicação
                                if (!publicacao.imagem.isNullOrEmpty()) {
                                    coil.compose.AsyncImage(
                                        model = publicacao.imagem,
                                        contentDescription = stringResource(R.string.user_post),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(4.dp)),
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(id = R.drawable.img),
                                        error = painterResource(id = R.drawable.img)
                                    )
                                } else {
                                    // Fallback para publicações sem imagem
                                    Image(
                                        painter = painterResource(id = R.drawable.img),
                                        contentDescription = stringResource(R.string.post_no_image),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(4.dp)),
                                        contentScale = ContentScale.Crop,
                                        alpha = 0.5f
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Mostrar mensagem quando não há publicações
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = stringResource(R.string.no_posts_icon),
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_posts_found),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.posts_will_appear),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditProfileScreenPreview() {
    MobileGYMBUDDYTheme {
        EditProfileScreen(navController = rememberNavController())
    }
}
