package senai.sp.jandira.mobile_gymbuddy.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import senai.sp.jandira.mobile_gymbuddy.R
import senai.sp.jandira.mobile_gymbuddy.data.repository.UserDataRepository
import senai.sp.jandira.mobile_gymbuddy.data.model.UsuarioCompleteRequest
import senai.sp.jandira.mobile_gymbuddy.data.service.RetrofitFactory
import senai.sp.jandira.mobile_gymbuddy.ui.theme.MobileGYMBUDDYTheme
import senai.sp.jandira.mobile_gymbuddy.ui.theme.secondaryLight
import senai.sp.jandira.mobile_gymbuddy.utils.UserPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImcScreen(navController: NavController) {
    val context = LocalContext.current
    val userDataRepository = UserDataRepository(context)
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val monoFont = FontFamily.Monospace

    // Estados para os campos de texto
    var peso by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    
    // Estados para o resultado do IMC
    var imcCalculado by remember { mutableStateOf<Double?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(8) }
    var progress by remember { mutableStateOf(0f) }

    // Estados para o Scaffold e Coroutine
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Função para obter a categoria do IMC
    fun getImcCategory(imc: Double): Pair<String, Color> {
        return when {
            imc < 18.5 -> "Abaixo do peso" to Color(0xFF4CAF50)
            imc < 25.0 -> "Peso normal" to Color(0xFF2196F3)
            imc < 30.0 -> "Sobrepeso" to Color(0xFFFF9800)
            imc < 35.0 -> "Obesidade Grau I" to Color(0xFFFF5722)
            else -> "Obesidade Grau II+" to Color(0xFFF44336)
        }
    }
    
    // Animação do progresso
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    // Scaffold é necessário para exibir o Snackbar
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Aplica o padding interno do Scaffold
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Centraliza todo o conteúdo
        ) {
            // Seção do Logo e Título
            val logoRes = if (isDarkTheme) R.drawable.logo_escuro else R.drawable.logo_claro
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = stringResource(id = R.string.logo_description),
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(id = R.string.imc_title),
                fontFamily = monoFont,
                fontSize = 32.sp,
                color = textColor
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Seção dos Campos de Entrada (Inputs)
            OutlinedTextField(
                value = peso,
                onValueChange = { novoValor ->
                    val valorFiltrado = novoValor.replace(',', '.')
                    if (valorFiltrado.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        peso = valorFiltrado
                    }
                },
                label = { Text(stringResource(id = R.string.your_weight)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text(stringResource(id = R.string.kg_unit)) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = secondaryLight,
                    unfocusedBorderColor = secondaryLight
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = altura,
                onValueChange = { novoValor ->
                    val valorFiltrado = novoValor.replace(',', '.')
                    if (valorFiltrado.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        altura = valorFiltrado
                    }
                },
                label = { Text(stringResource(id = R.string.your_height)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text(stringResource(id = R.string.m_unit)) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = secondaryLight,
                    unfocusedBorderColor = secondaryLight
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Seção do Botão de Ação
            Button(
                onClick = {
                    val pesoDouble = peso.toDoubleOrNull()
                    val alturaDouble = altura.toDoubleOrNull()
                    
                    android.util.Log.d("IMC_CONVERSAO", "Peso digitado: '$peso' -> $pesoDouble")
                    android.util.Log.d("IMC_CONVERSAO", "Altura digitada: '$altura' -> $alturaDouble")

                    if (pesoDouble != null && alturaDouble != null && alturaDouble > 0) {
                        // Inicia uma coroutine para tarefas assíncronas
                        scope.launch {
                            val imc = pesoDouble / (alturaDouble * alturaDouble)
                            imcCalculado = imc
                            showResult = true
                            timeLeft = 8
                            progress = 0f

                            // Recuperar dados temporários do usuário
                            val tempUserData = userDataRepository.getTemporaryUserData()
                            
                            android.util.Log.d("IMC_TEMP_DATA", "Dados temporários encontrados: ${tempUserData != null}")
                            if (tempUserData != null) {
                                android.util.Log.d("IMC_TEMP_DATA", "Nome: ${tempUserData.nome}")
                                android.util.Log.d("IMC_TEMP_DATA", "Email: ${tempUserData.email}")
                            }
                            
                            if (tempUserData != null) {
                                try {
                                    // Criar usuário completo com peso e altura
                                    val usuarioCompleto = UsuarioCompleteRequest(
                                        nome = tempUserData.nome,
                                        email = tempUserData.email,
                                        senha = tempUserData.senha,
                                        peso = pesoDouble,
                                        altura = alturaDouble,
                                        nickname = tempUserData.nickname,
                                        dataNascimento = tempUserData.dataNascimento,
                                        fotoPerfil = tempUserData.fotoPerfil
                                    )
                                    
                                    // Log detalhado dos dados que serão enviados
                                    android.util.Log.d("IMC_CADASTRO", "=== DADOS DO USUÁRIO COMPLETO ===")
                                    android.util.Log.d("IMC_CADASTRO", "Nome: ${usuarioCompleto.nome}")
                                    android.util.Log.d("IMC_CADASTRO", "Email: ${usuarioCompleto.email}")
                                    android.util.Log.d("IMC_CADASTRO", "Peso: ${usuarioCompleto.peso}")
                                    android.util.Log.d("IMC_CADASTRO", "Altura: ${usuarioCompleto.altura}")
                                    android.util.Log.d("IMC_CADASTRO", "Nickname: ${usuarioCompleto.nickname}")
                                    android.util.Log.d("IMC_CADASTRO", "IMC Calculado: $imc")
                                    
                                    // Enviar para API
                                    android.util.Log.d("IMC_CADASTRO", "Chamando cadastrarUsuarioCompleto...")
                                    val usuarioService = RetrofitFactory.getUsuarioService()
                                    val response = usuarioService.cadastrarUsuarioCompleto(usuarioCompleto)
                                    
                                    if (response.isSuccessful && response.body()?.statusCode == 200) {
                                        android.util.Log.d("IMC_CADASTRO", "Usuário cadastrado com sucesso!")
                                        
                                        // Fazer login automático após cadastro bem-sucedido
                                        try {
                                            android.util.Log.d("IMC_AUTO_LOGIN", "Iniciando login automático...")
                                            val loginResponse = usuarioService.logarUsuario(tempUserData.email, tempUserData.senha)
                                            
                                            if (loginResponse.isSuccessful) {
                                                val loginResult = loginResponse.body()
                                                if (loginResult != null && loginResult.status && !loginResult.usuario.isNullOrEmpty()) {
                                                    val usuario = loginResult.usuario[0]
                                                    
                                                    // Salvar dados do usuário usando UserPreferences
                                                    UserPreferences.saveUserData(
                                                        context = context,
                                                        id = usuario.id,
                                                        name = usuario.nome,
                                                        nickname = usuario.nickname,
                                                        email = usuario.email,
                                                        photoUrl = usuario.foto_perfil
                                                    )
                                                    
                                                    android.util.Log.d("IMC_AUTO_LOGIN", "Login automático bem-sucedido! Usuário: ${usuario.nome}")
                                                } else {
                                                    android.util.Log.e("IMC_AUTO_LOGIN", "Falha no login automático - dados inválidos")
                                                }
                                            } else {
                                                android.util.Log.e("IMC_AUTO_LOGIN", "Falha no login automático - erro ${loginResponse.code()}")
                                            }
                                        } catch (loginError: Exception) {
                                            android.util.Log.e("IMC_AUTO_LOGIN", "Erro no login automático: ${loginError.message}")
                                        }
                                        
                                        // Limpar dados temporários após sucesso
                                        userDataRepository.clearTemporaryData()
                                    } else {
                                        android.util.Log.e("IMC_CADASTRO", "Erro ao cadastrar: ${response.body()?.message}")
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("IMC_CADASTRO", "Erro na API: ${e.message}")
                                }
                            } else {
                                android.util.Log.w("IMC_CADASTRO", "Nenhum dado temporário encontrado")
                            }

                            // Timer com barra de progresso
                            repeat(8) { second ->
                                delay(1000)
                                timeLeft = 8 - (second + 1)
                                progress = (second + 1) / 8f
                            }

                            // Navega para a tela Home
                            navController.navigate("home?postSuccess=false")
                        }
                    } else {
                        // Lógica de erro caso os campos estejam inválidos ou vazios
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Por favor, preencha o peso e a altura corretamente.",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                },
                enabled = !showResult,
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = secondaryLight)
            ) {
                Text(text = stringResource(id = R.string.calculate))
            }
            
            // Resultado visual do IMC
            if (showResult && imcCalculado != null) {
                Spacer(modifier = Modifier.height(32.dp))
                
                ImcResultCard(
                    imc = imcCalculado!!,
                    timeLeft = timeLeft,
                    progress = animatedProgress,
                    isDarkTheme = isDarkTheme,
                    getImcCategory = ::getImcCategory
                )
            }
        }
    }
}

@Composable
fun ImcResultCard(
    imc: Double,
    timeLeft: Int,
    progress: Float,
    isDarkTheme: Boolean,
    getImcCategory: (Double) -> Pair<String, Color>
) {
    val (categoria, corCategoria) = getImcCategory(imc)
    
    // Cores de sucesso (verde)
    val successColor = Color(0xFF4CAF50)
    val successLightColor = Color(0xFF81C784)
    
    // Animação da cor da categoria
    val animatedColor by animateColorAsState(
        targetValue = corCategoria,
        animationSpec = tween(durationMillis = 500),
        label = "categoryColor"
    )
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ícone de sucesso
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = successColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(30.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = successColor
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Mensagem de sucesso compacta
                Text(
                    text = "IMC Calculado com Sucesso!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = successColor,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Valor do IMC - mais compacto
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "IMC: ",
                        fontSize = 18.sp,
                        color = if (isDarkTheme) Color.White else Color.Black
                    )
                    Text(
                        text = String.format("%.1f", imc),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = animatedColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = animatedColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = categoria,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Mensagem menor de redirecionamento
                Text(
                    text = "Redirecionando...",
                    fontSize = 13.sp,
                    color = if (isDarkTheme) Color.Gray else Color.DarkGray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Barra que diminui (invertida - mostra o tempo restante)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isDarkTheme) Color.Gray.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(1f - progress) // Barra que diminui
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(
                                        successColor,
                                        successLightColor
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ImcScreenPreview() {
    MobileGYMBUDDYTheme {
        ImcScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun ImcResultCardPreview() {
    MobileGYMBUDDYTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            ImcResultCard(
                imc = 23.5,
                timeLeft = 3,
                progress = 0.6f,
                isDarkTheme = false,
                getImcCategory = { imc ->
                    when {
                        imc < 18.5 -> "Abaixo do peso" to Color(0xFF4CAF50)
                        imc < 25.0 -> "Peso normal" to Color(0xFF2196F3)
                        imc < 30.0 -> "Sobrepeso" to Color(0xFFFF9800)
                        imc < 35.0 -> "Obesidade Grau I" to Color(0xFFFF5722)
                        else -> "Obesidade Grau II+" to Color(0xFFF44336)
                    }
                }
            )
        }
    }
}

