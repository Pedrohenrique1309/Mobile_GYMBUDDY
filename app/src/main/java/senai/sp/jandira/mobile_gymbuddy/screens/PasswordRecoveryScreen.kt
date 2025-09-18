package senai.sp.jandira.mobile_gymbuddy.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import senai.sp.jandira.mobile_gymbuddy.R
import senai.sp.jandira.mobile_gymbuddy.ui.theme.MobileGYMBUDDYTheme
import senai.sp.jandira.mobile_gymbuddy.ui.theme.secondaryLight

enum class RecoveryStep {
    ENTER_EMAIL, VERIFY_CODE, RESET_PASSWORD
}

val monoFont = FontFamily.Monospace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordRecoveryScreen(navController: NavController) {
    var step by remember { mutableStateOf(RecoveryStep.ENTER_EMAIL) }
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val buttonTextColor = if (isDarkTheme) Color.White else Color.Black

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        val logoRes = if (isDarkTheme) R.drawable.logo_escuro else R.drawable.logo_claro
        Image(
            painter = painterResource(id = logoRes),
            contentDescription = stringResource(id = R.string.logo_description),
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))

        when (step) {
            RecoveryStep.ENTER_EMAIL -> {
                StepEnterEmail(
                    onNext = { step = RecoveryStep.VERIFY_CODE },
                    textColor = textColor,
                    buttonTextColor = buttonTextColor
                )
            }
            RecoveryStep.VERIFY_CODE -> {
                StepVerifyCode(
                    onNext = { step = RecoveryStep.RESET_PASSWORD },
                    textColor = textColor,
                    buttonTextColor = buttonTextColor
                )
            }
            RecoveryStep.RESET_PASSWORD -> {
                StepResetPassword(
                    onFinish = { /* Redireciona para login ou dashboard */ },
                    textColor = textColor,
                    buttonTextColor = buttonTextColor
                )
            }
        }

        // Link para login no final da tela
        Spacer(modifier = Modifier.weight(1f))
        Text(
            "Voltar para login",
            color = secondaryLight,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable {
                navController.navigate("login")
            }
        )

        Spacer(modifier = Modifier.height(140.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepEnterEmail(
    onNext: () -> Unit,
    textColor: Color,
    buttonTextColor: Color
) {
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "RECUPERAÇÃO DE SENHA",
            fontSize = 22.sp,
            color = textColor,
            fontFamily = monoFont
        )

        Spacer(modifier = Modifier.height(64.dp))

        Text(
            "Enviaremos um código de verificação para seu email",
            color = textColor
        )

        Spacer(modifier = Modifier.height(64.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Digite seu email") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = secondaryLight,
                unfocusedBorderColor = secondaryLight
            )
        )

        Spacer(modifier = Modifier.height(82.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .width(200.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = secondaryLight)
        ) {
            Text("Receber código", color = buttonTextColor)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepVerifyCode(
    onNext: () -> Unit,
    textColor: Color,
    buttonTextColor: Color
) {
    var code by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "RECUPERAÇÃO DE SENHA",
            fontSize = 22.sp,
            color = textColor,
            fontFamily = monoFont
        )

        Spacer(modifier = Modifier.height(64.dp))

        Text(
            "Um email de recuperação foi enviado para ga******@gmail.com",
            color = secondaryLight
        )

        Spacer(modifier = Modifier.height(64.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            placeholder = { Text("Digite o código enviado") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = secondaryLight,
                unfocusedBorderColor = secondaryLight
            )
        )

        Spacer(modifier = Modifier.height(82.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .width(200.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = secondaryLight)
        ) {
            Text("Verificar", color = buttonTextColor)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepResetPassword(
    onFinish: () -> Unit,
    textColor: Color,
    buttonTextColor: Color
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "RECUPERAÇÃO DE SENHA",
            fontSize = 22.sp,
            color = textColor,
            fontFamily = monoFont
        )

        Spacer(modifier = Modifier.height(64.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Crie uma nova senha") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = secondaryLight,
                unfocusedBorderColor = secondaryLight
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = { Text("Confirmar nova senha") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = secondaryLight,
                unfocusedBorderColor = secondaryLight
            )
        )

        Spacer(modifier = Modifier.height(82.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .width(200.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = secondaryLight)
        ) {
            Text("Alterar senha", color = buttonTextColor)
        }
    }
}

//// ---------- PREVIEWS ----------

@Preview(showBackground = true)
@Composable
fun PasswordRecoveryScreenPreview() {
    MobileGYMBUDDYTheme {
        PasswordRecoveryScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun StepEnterEmailPreview() {
    MobileGYMBUDDYTheme {
        StepEnterEmail(onNext = {}, textColor = Color.Black, buttonTextColor = Color.Black)
    }
}

@Preview(showBackground = true)
@Composable
fun StepVerifyCodePreview() {
    MobileGYMBUDDYTheme {
        StepVerifyCode(onNext = {}, textColor = Color.Black, buttonTextColor = Color.Black)
    }
}

@Preview(showBackground = true)
@Composable
fun StepResetPasswordPreview() {
    MobileGYMBUDDYTheme {
        StepResetPassword(onFinish = {}, textColor = Color.Black, buttonTextColor = Color.Black)
    }
}