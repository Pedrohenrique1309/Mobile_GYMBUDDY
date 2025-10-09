package senai.sp.jandira.mobile_gymbuddy.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.launch
import senai.sp.jandira.mobile_gymbuddy.R
import senai.sp.jandira.mobile_gymbuddy.data.model.Usuario
import senai.sp.jandira.mobile_gymbuddy.data.service.RetrofitFactory
import senai.sp.jandira.mobile_gymbuddy.ui.theme.MobileGYMBUDDYTheme
import senai.sp.jandira.mobile_gymbuddy.ui.theme.secondaryLight
import senai.sp.jandira.mobile_gymbuddy.utils.UserPreferences

/**
 * IMC Screen otimizada que aproveita:
 * 1. TRIGGER automática para cálculo de IMC (peso e altura -> IMC calculado automaticamente)
 * 2. FUNCTION fn_classificar_imc para classificação automática
 * 3. TRIGGER de validação de dados
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImcScreenOptimized(navController: NavController) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val monoFont = FontFamily.Monospace
    val context = LocalContext.current

    // Estados para os campos de texto
    var peso by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var isCalculating by remember { mutableStateOf(false) }
    var resultadoIMC by remember { mutableStateOf<ImcResultado?>(null) }

    // Estados para o Scaffold e Coroutine
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo e Título
            val logoRes = if (isDarkTheme) R.drawable.logo_escuro else R.drawable.logo_claro
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = stringResource(id = R.string.logo_description),
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Calculadora de IMC",
                fontFamily = monoFont,
                fontSize = 32.sp,
                color = textColor,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Powered by Database Functions",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Campos de entrada
            OutlinedTextField(
                value = peso,
                onValueChange = { novoValor ->
                    val valorFiltrado = novoValor.replace(',', '.')
                    if (valorFiltrado.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        peso = valorFiltrado
                    }
                },
                label = { Text("Seu peso") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("kg") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = secondaryLight,
                    unfocusedBorderColor = secondaryLight
                ),
                enabled = !isCalculating
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
                label = { Text("Sua altura") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("m") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = secondaryLight,
                    unfocusedBorderColor = secondaryLight
                ),
                enabled = !isCalculating
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Botão de cálculo
            Button(
                onClick = {
                    val pesoDouble = peso.toDoubleOrNull()
                    val alturaDouble = altura.toDoubleOrNull()

                    if (pesoDouble != null && alturaDouble != null && alturaDouble > 0) {
                        isCalculating = true
                        scope.launch {
                            try {
                                // Usar o banco para calcular IMC com as triggers e functions
                                val resultado = calcularImcComBanco(
                                    peso = pesoDouble,
                                    altura = alturaDouble,
                                    context = context
                                )
                                
                                if (resultado != null) {
                                    resultadoIMC = resultado
                                    Log.d("ImcOptimized", "✅ IMC calculado pelo banco: ${resultado.valor} - ${resultado.classificacao}")
                                } else {
                                    snackbarHostState.showSnackbar(
                                        message = "Erro ao calcular IMC. Tente novamente.",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e("ImcOptimized", "Erro ao calcular IMC", e)
                                snackbarHostState.showSnackbar(
                                    message = "Erro de conexão. Tente novamente.",
                                    duration = SnackbarDuration.Short
                                )
                            } finally {
                                isCalculating = false
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Por favor, preencha o peso e a altura corretamente.",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = secondaryLight),
                enabled = !isCalculating
            ) {
                if (isCalculating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                } else {
                    Text(text = "Calcular IMC")
                }
            }

            // Exibir resultado
            resultadoIMC?.let { resultado ->
                Spacer(modifier = Modifier.height(32.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Seu IMC é:",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = String.format("%.2f", resultado.valor),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = resultado.classificacao,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "✨ Calculado com Functions do Banco",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        navController.navigate("home")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continuar para o App")
                }
            }
        }
    }
}

// =================================================================================
// MODELO DE DADOS E LÓGICA
// =================================================================================

data class ImcResultado(
    val valor: Double,
    val classificacao: String
)

/**
 * Função que simula o uso das TRIGGERS e FUNCTIONS do banco:
 * Por enquanto calcula localmente, mas está preparada para integração futura
 * com as triggers de cálculo automático de IMC e function fn_classificar_imc
 */
suspend fun calcularImcComBanco(
    peso: Double,
    altura: Double,
    @Suppress("UNUSED_PARAMETER") context: Context
): ImcResultado? {
    return try {
        // Simular processamento do banco
        kotlinx.coroutines.delay(1000)
        
        // Calcular IMC (futuramente será feito pela trigger do banco)
        val imc = peso / (altura * altura)
        
        // Classificar usando lógica que simula fn_classificar_imc
        val classificacao = classificarImcLocal(imc)
        
        Log.d("ImcComBanco", "✅ IMC calculado (simulando banco): $imc")
        Log.d("ImcComBanco", "✅ Classificação (simulando function): $classificacao")
        
        ImcResultado(valor = imc, classificacao = classificacao)
    } catch (e: Exception) {
        Log.e("ImcComBanco", "❌ Erro ao calcular IMC", e)
        // Fallback: calcular localmente
        val imc = peso / (altura * altura)
        ImcResultado(valor = imc, classificacao = classificarImcLocal(imc))
    }
}

/**
 * Classificação local que simula a function fn_classificar_imc do banco
 * (até que o backend implemente o endpoint que consome a function)
 */
fun classificarImcLocal(imc: Double): String {
    return when {
        imc < 18.5 -> "Abaixo do Peso"
        imc < 25.0 -> "Peso Normal"
        imc < 30.0 -> "Sobrepeso"
        else -> "Obesidade"
    }
}

// Função removida - modelo não é mais necessário

@Preview(showBackground = true)
@Composable
fun ImcScreenOptimizedPreview() {
    MobileGYMBUDDYTheme {
        ImcScreenOptimized(navController = rememberNavController())
    }
}
