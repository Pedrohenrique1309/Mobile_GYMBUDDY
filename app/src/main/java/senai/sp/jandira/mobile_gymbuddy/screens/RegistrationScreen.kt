package senai.sp.jandira.mobile_gymbuddy.screens

import android.util.Log
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import senai.sp.jandira.mobile_gymbuddy.R
import senai.sp.jandira.mobile_gymbuddy.ui.theme.MobileGYMBUDDYTheme
import senai.sp.jandira.mobile_gymbuddy.ui.theme.secondaryLight
import senai.sp.jandira.mobile_gymbuddy.data.model.Usuario
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onRegisterClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    navController: NavHostController
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val monoFont = FontFamily.Monospace

    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var confirmEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }

    val errorEmptyFields = stringResource(id = R.string.error_empty_fields)
    val errorEmailsNotMatching = stringResource(id = R.string.error_emails_not_matching)
    val errorPasswordsNotMatching = stringResource(id = R.string.error_passwords_not_matching)
    val errorPasswordRequirements = stringResource(id = R.string.error_password_requirements)

    val passwordRegex = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%^&*()_+-/])(?=.{8,}).*\$".toRegex()

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = errorMessage) {
        if (errorMessage.isNotEmpty()) {
            delay(3000)
            errorMessage = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        val logoRes = if (isDarkTheme) R.drawable.logo_escuro else R.drawable.logo_claro
        Image(
            painter = painterResource(id = logoRes),
            contentDescription = stringResource(id = R.string.logo_description),
            modifier = Modifier.size(150.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = stringResource(id = R.string.register_title),
            fontFamily = monoFont,
            fontSize = 32.sp,
            color = textColor
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo de texto para o nickname
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Crie um nome de usuário") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = secondaryLight,
                unfocusedBorderColor = secondaryLight
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(id = R.string.insert_email)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = secondaryLight,
                unfocusedBorderColor = secondaryLight
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmEmail,
            onValueChange = { confirmEmail = it },
            label = { Text(stringResource(id = R.string.confirm_email)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = secondaryLight,
                unfocusedBorderColor = secondaryLight
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(id = R.string.create_password)) },
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

        Text(
            text = stringResource(id = R.string.error_password_requirements),
            fontSize = 11.sp,
            color = textColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(stringResource(id = R.string.confirm_password)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle confirmar senha")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = secondaryLight,
                unfocusedBorderColor = secondaryLight
            )
        )

        Spacer(modifier = Modifier.height(26.dp))

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

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                // A validação agora checa se o nickname não está vazio
                if (nickname.isBlank() || email.isBlank() || confirmEmail.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    errorMessage = errorEmptyFields
                } else if (email != confirmEmail) {
                    errorMessage = errorEmailsNotMatching
                } else if (password != confirmPassword) {
                    errorMessage = errorPasswordsNotMatching
                } else if (!password.matches(passwordRegex)) {
                    errorMessage = errorPasswordRequirements
                } else {
                    errorMessage = ""
                    scope.launch {
                        try {
                            val usuarioService = RetrofitFactory.getUsuarioService()
                            // O objeto Usuario agora inclui o campo 'nickname'
                            val novoUsuario = Usuario(
                                nome = null, // Passe o valor nulo explicitamente
                                email = email,
                                senha = password,
                                nickname = nickname
                            )

                            val response = usuarioService.cadastrarUsuario(novoUsuario)

                            if (response.isSuccessful) {
                                navController.navigate("login")
                            } else {
                                val errorBody = response.errorBody()?.string()
                                Log.e("API_ERROR", "Error: ${response.code()} - $errorBody")
                                errorMessage = "Erro no cadastro. Verifique os dados."
                            }
                        } catch (e: Exception) {
                            Log.e("API_CONNECTION", "Erro de conexão: ${e.message}")
                            errorMessage = "Não foi possível conectar. Tente novamente mais tarde."
                        }
                    }
                }
            },
            modifier = Modifier
                .width(200.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = secondaryLight
            )
        ) {
            Text(
                text = stringResource(id = R.string.register_button),
                color = if (isDarkTheme) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.have_account),
                color = textColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(id = R.string.login_here),
                color = secondaryLight,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onLoginClick() }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {
    MobileGYMBUDDYTheme {
        RegistrationScreen(
            onRegisterClick = {},
            onLoginClick = {},
            navController = rememberNavController()
        )
    }
}