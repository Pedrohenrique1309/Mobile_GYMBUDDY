package senai.sp.jandira.mobile_gymbuddy.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import senai.sp.jandira.mobile_gymbuddy.R
import senai.sp.jandira.mobile_gymbuddy.data.model.PublicacaoRequest
import senai.sp.jandira.mobile_gymbuddy.data.service.RetrofitFactory
import senai.sp.jandira.mobile_gymbuddy.ui.theme.secondaryLight
import senai.sp.jandira.mobile_gymbuddy.utils.AzureBlobUploader
import senai.sp.jandira.mobile_gymbuddy.utils.UserPreferences
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishingScreen(navController: NavController) {
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var showImagePickerSheet by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val darkTheme = isSystemInDarkTheme()
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Pegar o ID do usuário logado usando UserPreferences
    val currentUserId = UserPreferences.getUserId(context)
    android.util.Log.d("PublishingScreen", "ID do usuário logado: $currentUserId")

    // Criar arquivo temporário para a foto
    val createImageFile = {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = File(context.cacheDir, "images")
        if (!storageDir.exists()) storageDir.mkdirs()
        File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    // Launcher para câmera (foto)
    val cameraPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = photoUri
        }
        showImagePickerSheet = false
    }

    // Launcher para câmera (vídeo)
    val cameraVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
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

    val logoRes = if (darkTheme) R.drawable.logo_escuro else R.drawable.logo_claro
    val backgroundColor = if (darkTheme) Color.Black else Color.White
    val surfaceColor = if (darkTheme) Color(0xFF2A2A2A) else Color(0xFFE0E0E0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = if (darkTheme) Color.White else Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "NOVA PUBLICAÇÃO",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (darkTheme) Color.White else Color.Black
            )

            Image(
                painter = painterResource(id = logoRes),
                contentDescription = "Logo",
                modifier = Modifier.height(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Área da imagem
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(surfaceColor)
                .border(
                    width = 1.dp,
                    color = Color.Gray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { showImagePickerSheet = true },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Imagem selecionada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Adicionar imagem",
                    tint = Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Label Descrição
        Text(
            text = "Descrição:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Red,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de descrição
        OutlinedTextField(
            value = description,
            onValueChange = {
                if (it.length <= 800) {
                    description = it
                }
            },
            placeholder = {
                Text(
                    text = "Crie uma descrição para sua publicação...",
                    color = Color.Gray
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = surfaceColor,
                unfocusedContainerColor = surfaceColor
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Contador de caracteres
        Text(
            text = "${description.length}/800",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Label Localização
        Text(
            text = "Localização (opcional):",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (darkTheme) Color.White else Color.Black,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de localização editável
        OutlinedTextField(
            value = location,
            onValueChange = {
                if (it.length <= 100) {
                    location = it
                }
            },
            placeholder = {
                Text(
                    text = "Digite a localização...",
                    color = Color.Gray
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Localização",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = surfaceColor,
                unfocusedContainerColor = surfaceColor
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Contador de caracteres para localização
        Text(
            text = "${location.length}/100",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Mensagem de erro
        errorMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Red.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = message,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        
        // Botão Publicar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    if (selectedImageUri != null && description.isNotBlank()) {
                        coroutineScope.launch {
                            isUploading = true
                            errorMessage = null
                            
                            try {
                                // 1. Upload da imagem para Azure Blob Storage
                                val imageUrl = AzureBlobUploader.uploadImage(context, selectedImageUri!!)
                                
                                if (imageUrl != null) {
                                    // 2. Criar publicação no banco
                                    val dataAtual = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                    
                                    val publicacaoRequest = PublicacaoRequest(
                                        imagem = imageUrl,
                                        descricao = description,
                                        localizacao = location.ifBlank { null },
                                        data = dataAtual,
                                        idUser = currentUserId
                                    )
                                    
                                    val publicacaoService = RetrofitFactory.getPublicacaoService()
                                    val response = publicacaoService.criarPublicacao(publicacaoRequest)
                                    
                                    if (response.isSuccessful && response.body()?.statusCode == 200) {
                                        // Sucesso - voltar para tela anterior
                                        navController.popBackStack()
                                    } else {
                                        val errorMsg = response.body()?.message ?: "Erro desconhecido"
                                        errorMessage = "Erro ao criar publicação: $errorMsg"
                                    }
                                } else {
                                    errorMessage = "Erro ao fazer upload da imagem"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Erro inesperado: ${e.message}"
                            } finally {
                                isUploading = false
                            }
                        }
                    } else {
                        errorMessage = "Selecione uma imagem e adicione uma descrição"
                    }
                },
                enabled = !isUploading && selectedImageUri != null && description.isNotBlank(),
                modifier = Modifier
                    .width(200.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB71C1C),
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                if (isUploading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Publicando...",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                } else {
                    Text(
                        text = "Publicar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Bottom Sheet para seleção de imagem
    if (showImagePickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImagePickerSheet = false },
            sheetState = sheetState,
            containerColor = if (darkTheme) Color(0xFF1A1A1A) else Color.White,
            dragHandle = null
        ) {
            ImagePickerBottomSheetContent(
                onDismiss = { showImagePickerSheet = false },
                onCameraClick = {
                    try {
                        val imageFile = createImageFile()
                        photoUri = FileProvider.getUriForFile(
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
                onVideoClick = {
                    try {
                        val videoFile = createImageFile() // Reutilizando a função, mas para vídeo
                        photoUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            videoFile
                        )
                        cameraVideoLauncher.launch(photoUri!!)
                    } catch (e: Exception) {
                        showImagePickerSheet = false
                    }
                },
                darkTheme = darkTheme
            )
        }
    }
}

@Composable
fun ImagePickerBottomSheetContent(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onVideoClick: () -> Unit,
    darkTheme: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (darkTheme) Color(0xFF1A1A1A) else Color.White)
            .padding(16.dp)
    ) {
        // Header com X
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = onDismiss
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fechar",
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid de opções
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
                            color = secondaryLight,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Foto",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "FOTO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (darkTheme) Color.White else Color.Black
                )
            }

            // Opção VÍDEO
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onVideoClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = secondaryLight,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Vídeo",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "VÍDEO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (darkTheme) Color.White else Color.Black
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
                            color = secondaryLight,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Galeria",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "GALERIA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (darkTheme) Color.White else Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}


// =================================================================================
// PREVIEWS
// =================================================================================

// Preview para o Modo Claro (Light Mode)
@Preview(
    name = "Light Mode",
    showBackground = true
)
@Composable
fun PublishingScreenPreviewLight() {
    // Usamos rememberNavController() para simular o NavController no preview
    val navController = rememberNavController()
    PublishingScreen(navController = navController)
}

// Preview para o Modo Escuro (Dark Mode)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES // Configuração que força o tema escuro
)
@Composable
fun PublishingScreenPreviewDark() {
    val navController = rememberNavController()
    PublishingScreen(navController = navController)
}

// Preview apenas para o conteúdo do Bottom Sheet
@Preview(
    name = "Image Picker Bottom Sheet",
    showBackground = true
)
@Composable
fun ImagePickerBottomSheetContentPreview() {
    ImagePickerBottomSheetContent(
        onDismiss = {},
        onCameraClick = {},
        onGalleryClick = {},
        onVideoClick = {},
        darkTheme = false // Mude para true para ver a versão escura
    )
}