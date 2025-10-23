package senai.sp.jandira.mobile_gymbuddy.screens

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import senai.sp.jandira.mobile_gymbuddy.data.model.UsuarioUpdateRequest
import senai.sp.jandira.mobile_gymbuddy.utils.UserPreferences
import senai.sp.jandira.mobile_gymbuddy.utils.AzureBlobUploader
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController
) {
    val isDarkTheme = isSystemInDarkTheme()
    val logoRes = if (isDarkTheme) R.drawable.logo_escuro else R.drawable.logo_claro
    
    // Estados para os campos de edição
    var nomeUsuario by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var localizacao by remember { mutableStateOf("") }
    
    // Estados para seleção de foto
    var showImagePickerSheet by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoUpdateTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    val sheetState = rememberModalBottomSheetState()
    
    // Estados para carregar dados do usuário
    val context = LocalContext.current
    
    // Função para filtrar apenas letras e espaços
    fun filterOnlyLetters(input: String): String {
        return input.filter { char ->
            char.isLetter() || char.isWhitespace()
        }
    }
    
    // Função para criar arquivo de imagem temporário
    fun createImageFile(): File {
        val timeStamp = System.currentTimeMillis()
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(null)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
    
    // Launcher para câmera
    val cameraPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = photoUri
        }
        showImagePickerSheet = false
    }
    
    // Launcher para galeria
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
        showImagePickerSheet = false
    }
    val coroutineScope = rememberCoroutineScope()
    var usuarioDetalhes by remember { mutableStateOf<UsuarioDetalhes?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Estados para controle da atualização
    var isUpdating by remember { mutableStateOf(false) }
    var updateSuccess by remember { mutableStateOf(false) }
    var updateError by remember { mutableStateOf<String?>(null) }
    
    // Função para atualizar perfil
    fun atualizarPerfil() {
        if (nomeUsuario.isBlank() || email.isBlank()) {
            updateError = "Nome e email são obrigatórios"
            return
        }
        
        coroutineScope.launch {
            try {
                isUpdating = true
                updateError = null
                
                val userId = UserPreferences.getUserId(context)
                val usuarioService = RetrofitFactory.getUsuarioService()
                
                // 1. Upload da imagem se uma nova foi selecionada
                var imageUrl = usuarioDetalhes?.foto // URL atual da foto
                if (selectedImageUri != null) {
                    android.util.Log.d("EditProfile", "Fazendo upload da nova foto de perfil...")
                    val uploadedUrl = AzureBlobUploader.uploadImage(context, selectedImageUri!!)
                    if (uploadedUrl != null) {
                        imageUrl = uploadedUrl
                        android.util.Log.d("EditProfile", "Upload bem-sucedido: $uploadedUrl")
                    } else {
                        updateError = "Erro ao fazer upload da imagem"
                        return@launch
                    }
                }
                
                // 2. Atualizar dados do usuário
                val updateRequest = UsuarioUpdateRequest(
                    nome = nomeUsuario.trim(),
                    email = email.trim(),
                    senha = usuarioDetalhes?.senha ?: "123456", // Garantir que não seja vazio
                    peso = usuarioDetalhes?.peso ?: 0.0, // Garantir valor padrão
                    altura = usuarioDetalhes?.altura ?: 0.0, // Garantir valor padrão
                    imc = usuarioDetalhes?.imc ?: 0.0, // Garantir valor padrão
                    nickname = usuarioDetalhes?.nickname ?: "user",
                    dataNascimento = usuarioDetalhes?.dataNascimento,
                    foto = imageUrl ?: "",
                    descricao = if (descricao.isBlank()) "" else descricao.trim(),
                    localizacao = if (localizacao.isBlank()) "" else localizacao.trim(),
                    isBloqueado = usuarioDetalhes?.isBloqueado ?: 0
                )
                
                android.util.Log.d("EditProfile", "Dados sendo enviados:")
                android.util.Log.d("EditProfile", "Nome: ${updateRequest.nome}")
                android.util.Log.d("EditProfile", "Email: ${updateRequest.email}")
                android.util.Log.d("EditProfile", "Senha: [OCULTA]")
                android.util.Log.d("EditProfile", "Peso: ${updateRequest.peso}")
                android.util.Log.d("EditProfile", "Altura: ${updateRequest.altura}")
                android.util.Log.d("EditProfile", "IMC: ${updateRequest.imc}")
                android.util.Log.d("EditProfile", "Nickname: ${updateRequest.nickname}")
                android.util.Log.d("EditProfile", "Data Nasc: ${updateRequest.dataNascimento}")
                android.util.Log.d("EditProfile", "Foto: ${updateRequest.foto}")
                android.util.Log.d("EditProfile", "Descrição: ${updateRequest.descricao}")
                android.util.Log.d("EditProfile", "Localização: ${updateRequest.localizacao}")
                android.util.Log.d("EditProfile", "Bloqueado: ${updateRequest.isBloqueado}")
                android.util.Log.d("EditProfile", "Atualizando dados do usuário...")
                val response = usuarioService.atualizarUsuario(userId, updateRequest)
                
                if (response.isSuccessful && response.body() != null) {
                    val updateResponse = response.body()!!
                    if (updateResponse.statusCode == 200 && updateResponse.item) {
                        updateSuccess = true
                        android.util.Log.d("EditProfile", "Perfil atualizado com sucesso!")
                        
                        // Atualizar o estado local com a nova foto
                        usuarioDetalhes = usuarioDetalhes?.copy(foto = imageUrl)
                        
                        // Limpar a imagem selecionada para mostrar a nova foto do perfil
                        selectedImageUri = null
                        
                        // Atualizar timestamp para forçar recomposição da imagem
                        photoUpdateTimestamp = System.currentTimeMillis()
                        
                        // Atualizar dados salvos no SharedPreferences
                        UserPreferences.saveUserData(
                            context = context,
                            id = userId,
                            name = nomeUsuario.trim(),
                            nickname = usuarioDetalhes?.nickname ?: "user",
                            email = email.trim(),
                            photoUrl = imageUrl
                        )
                        
                        // Voltar para tela de perfil após 1.5 segundos
                        kotlinx.coroutines.delay(1500)
                        navController.popBackStack()
                    } else {
                        updateError = updateResponse.message
                    }
                } else {
                    updateError = "Erro ao atualizar perfil: ${response.code()}"
                }
            } catch (e: Exception) {
                android.util.Log.e("EditProfile", "Erro durante atualização", e)
                updateError = "Erro de conexão: ${e.message}"
            } finally {
                isUpdating = false
            }
        }
    }
    
    // Carregar dados do usuário para preencher os campos
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val userId = UserPreferences.getUserId(context)
                val usuarioService = RetrofitFactory.getUsuarioService()
                
                val userResponse = usuarioService.buscarUsuarioPorId(userId)
                
                if (userResponse.isSuccessful && userResponse.body() != null) {
                    val apiResponse = userResponse.body()!!
                    if (apiResponse.status && apiResponse.usuario.isNotEmpty()) {
                        usuarioDetalhes = apiResponse.usuario[0]
                        // Preencher os campos com os dados do usuário
                        nomeUsuario = usuarioDetalhes!!.nome
                        descricao = usuarioDetalhes!!.descricao ?: ""
                        email = usuarioDetalhes!!.email
                        localizacao = usuarioDetalhes!!.localizacao ?: ""
                    } else {
                        errorMessage = context.getString(R.string.user_not_found)
                    }
                } else {
                    errorMessage = context.getString(R.string.profile_load_error, userResponse.code())
                }
            } catch (e: Exception) {
                errorMessage = context.getString(R.string.connection_error, e.message ?: "")
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (isDarkTheme) {
                        Text(
                            text = stringResource(R.string.edit_profile_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    } else {
                        Image(
                            painter = painterResource(id = logoRes),
                            contentDescription = stringResource(R.string.logo_gym_buddy_desc),
                            modifier = Modifier
                                .height(72.dp)
                                .wrapContentWidth(),
                            contentScale = ContentScale.Fit
                        )
                    }
                },
                navigationIcon = {
                    if (isDarkTheme) {
                        IconButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back_button),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                // Loading State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else if (errorMessage != null) {
                // Error State
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                }
            } else {
                // Profile Photo Section
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.clickable { showImagePickerSheet = true }
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            coil.compose.AsyncImage(
                                model = selectedImageUri,
                                contentDescription = stringResource(R.string.profile_photo),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else if (usuarioDetalhes?.foto?.isNotEmpty() == true) {
                            coil.compose.AsyncImage(
                                model = usuarioDetalhes!!.foto + "?t=$photoUpdateTimestamp", // Usa timestamp controlado para evitar cache
                                contentDescription = stringResource(R.string.profile_photo),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                error = painterResource(R.drawable.ic_launcher_foreground)
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
                    
                    // Camera Icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = stringResource(R.string.camera_edit_icon),
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Form Fields
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Nome de usuário
                    Column {
                        Text(
                            text = stringResource(R.string.username_label),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        OutlinedTextField(
                            value = nomeUsuario,
                            onValueChange = { newValue -> 
                                nomeUsuario = filterOnlyLetters(newValue)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                focusedContainerColor = if (isDarkTheme) 
                                    MaterialTheme.colorScheme.surface 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = if (isDarkTheme) 
                                    MaterialTheme.colorScheme.surface 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    
                    // Descrição
                    Column {
                        Text(
                            text = stringResource(R.string.description_field_label),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        OutlinedTextField(
                            value = descricao,
                            onValueChange = { descricao = it },
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.description_edit_placeholder),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                focusedContainerColor = if (isDarkTheme) 
                                    MaterialTheme.colorScheme.surface 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = if (isDarkTheme) 
                                    MaterialTheme.colorScheme.surface 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 4
                        )
                    }
                    
                    // Email
                    Column {
                        Text(
                            text = stringResource(R.string.email_field_label),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                focusedContainerColor = if (isDarkTheme) 
                                    MaterialTheme.colorScheme.surface 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = if (isDarkTheme) 
                                    MaterialTheme.colorScheme.surface 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    
                    // Localização
                    Column {
                        Text(
                            text = stringResource(R.string.location_field_label),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        OutlinedTextField(
                            value = localizacao,
                            onValueChange = { localizacao = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                focusedContainerColor = if (isDarkTheme) 
                                    MaterialTheme.colorScheme.surface 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = if (isDarkTheme) 
                                    MaterialTheme.colorScheme.surface 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Buttons Section
                // Confirmar Edição Button
                Button(
                    onClick = { atualizarPerfil() },
                    enabled = !isUpdating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isUpdating) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onSecondary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Salvando...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.confirm_edit_button),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Feedback de sucesso ou erro
                updateError?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Erro",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                if (updateSuccess) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Sucesso",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Perfil atualizado com sucesso!",
                                color = Color(0xFF4CAF50),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Bottom Sheet para seleção de foto
    if (showImagePickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImagePickerSheet = false },
            sheetState = sheetState,
            containerColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White,
            dragHandle = null
        ) {
            PhotoPickerBottomSheetContent(
                onDismiss = { showImagePickerSheet = false },
                onCameraClick = {
                    try {
                        val imageFile = createImageFile()
                        photoUri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            imageFile
                        )
                        cameraPhotoLauncher.launch(photoUri!!)
                    } catch (e: Exception) {
                        showImagePickerSheet = false
                    }
                },
                onGalleryClick = {
                    galleryLauncher.launch("image/*")
                },
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@Composable
fun PhotoPickerBottomSheetContent(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    isDarkTheme: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header com título e botão fechar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.select_profile_photo),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) Color.White else Color.Black
            )
            
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close_photo_picker),
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid de opções (apenas FOTO e GALERIA)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Opção FOTO
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onCameraClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondary,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = stringResource(R.string.camera_icon),
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.photo_option),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color.Black
                )
            }

            // Opção GALERIA
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onGalleryClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondary,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = stringResource(R.string.gallery_icon),
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.gallery_option),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color.Black
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
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
