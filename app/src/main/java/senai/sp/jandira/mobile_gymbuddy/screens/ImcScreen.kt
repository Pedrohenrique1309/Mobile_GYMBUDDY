package senai.sp.jandira.mobile_gymbuddy.screens

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import senai.sp.jandira.mobile_gymbuddy.R
import senai.sp.jandira.mobile_gymbuddy.ui.theme.MobileGYMBUDDYTheme
import senai.sp.jandira.mobile_gymbuddy.ui.theme.secondaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImcScreen(navController: NavController) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val monoFont = FontFamily.Monospace

    // Estados para os campos de texto
    var peso by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }

    // Estados para o Scaffold e Coroutine
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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

                    if (pesoDouble != null && alturaDouble != null && alturaDouble > 0) {
                        // Inicia uma coroutine para tarefas assíncronas
                        scope.launch {
                            val imc = pesoDouble / (alturaDouble * alturaDouble)

                            // Prepara a mensagem usando o recurso de string
                            val mensagem = navController.context.getString(R.string.imc_message, imc)

                            // 1. Mostra a mensagem do IMC no Snackbar
                            snackbarHostState.showSnackbar(
                                message = mensagem,
                                duration = SnackbarDuration.Long
                            )

                            // 2. Espera 5 segundos
                            delay(5000)

                            // 3. Navega para a tela Home
                            navController.navigate("home")
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
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = secondaryLight)
            ) {
                Text(text = stringResource(id = R.string.calculate))
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

