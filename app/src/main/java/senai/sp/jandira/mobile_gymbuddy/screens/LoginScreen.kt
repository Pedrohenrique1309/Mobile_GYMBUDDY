package senai.sp.jandira.mobile_gymbuddy.screens

import android.util.Log
import senai.sp.jandira.mobile_gymbuddy.data.service.RetrofitFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import senai.sp.jandira.mobile_gymbuddy.R
// CORREÇÃO AQUI: Caminho do import ajustado
import senai.sp.jandira.mobile_gymbuddy.ui.theme.MobileGYMBUDDYTheme
import senai.sp.jandira.mobile_gymbuddy.ui.theme.onPrimaryLight
import senai.sp.jandira.mobile_gymbuddy.ui.theme.secondaryLight


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onCreateAccountClick: () -> Unit = {},
    navController: NavController
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val monoFont = FontFamily.Monospace

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val errorEmptyFields = stringResource(id = R.string.error_empty_fields)
    // Agora esta linha vai funcionar, pois a string existe em strings.xml
    val errorInvalidCredentials = stringResource(id = R.string.error_invalid_credentials)

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = errorMessage) {
        if (errorMessage.isNotEmpty()) {
            delay(5000)
            errorMessage = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ... O resto do código continua o mesmo ...
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val logoRes = if (isDarkTheme) R.drawable.logo_escuro else R.drawable.logo_claro
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = stringResource(id = R.string.logo_description),
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.login_title),
                fontFamily = monoFont,
                fontSize = 32.sp,
                color = textColor
            )

            Spacer(modifier = Modifier.height(78.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(id = R.string.email_or_user)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = secondaryLight,
                    unfocusedBorderColor = secondaryLight
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(id = R.string.password)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle senha")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = secondaryLight,
                    unfocusedBorderColor = secondaryLight
                )
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = errorMessage.isNotEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Erro",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.forgot_password),
                color = secondaryLight,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onForgotPasswordClick() }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = errorEmptyFields
                    } else {
                        errorMessage = ""
                        scope.launch {
                            try {
                                val usuarioService = RetrofitFactory.getUsuarioService()
                                val response = usuarioService.logarUsuario(email, password)

                                if (response.isSuccessful) {
                                    val loginResponse = response.body()
                                    if (loginResponse != null && loginResponse.status && !loginResponse.usuario.isNullOrEmpty()) {
                                        Log.d("LOGIN_SUCCESS", "Login bem-sucedido: ${loginResponse.usuario[0].nome}")
                                        navController.navigate("home")
                                    } else {
                                        errorMessage = errorInvalidCredentials
                                    }
                                } else {
                                    Log.e("LOGIN_ERROR", "Erro ${response.code()}: ${response.errorBody()?.string()}")
                                    errorMessage = errorInvalidCredentials
                                }
                            } catch (e: Exception) {
                                Log.e("LOGIN_CONNECTION", "Falha de conexão: ${e.message}", e)
                                errorMessage = "Falha na conexão. Tente novamente."
                            }
                        }
                    }
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = secondaryLight)
            ) {
                Text(
                    text = stringResource(id = R.string.login_button),
                    color = onPrimaryLight
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            Text(
                text = stringResource(id = R.string.no_account),
                color = textColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(id = R.string.create_account),
                color = secondaryLight,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onCreateAccountClick() }
            )
        }
    }
}