package senai.sp.jandira.mobile_gymbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import senai.sp.jandira.mobile_gymbuddy.screens.HomeScreen
import senai.sp.jandira.mobile_gymbuddy.screens.ImcScreen // <-- MUDANÇA: Importe da nova tela
import senai.sp.jandira.mobile_gymbuddy.screens.LoginScreen
import senai.sp.jandira.mobile_gymbuddy.screens.PasswordRecoveryScreen
import senai.sp.jandira.mobile_gymbuddy.screens.RegistrationScreen
import senai.sp.jandira.mobile_gymbuddy.ui.theme.MobileGYMBUDDYTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileGYMBUDDYTheme {
                val navController = rememberNavController()
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    LoadingScreen(onTimeout = { showSplash = false })
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "login"
                        ) {
                            composable("login") {
                                LoginScreen(
                                    navController = navController,
                                    onForgotPasswordClick = { navController.navigate("password_recovery") },
                                    onCreateAccountClick = { navController.navigate("registration") }
                                )
                            }
                            composable("password_recovery") {
                                PasswordRecoveryScreen(navController = navController)
                            }
                            composable("registration") {
                                RegistrationScreen(
                                    navController = navController,
                                    onLoginClick = { navController.navigate("login") }
                                )
                            }
                            composable("home") {
                                HomeScreen(navController = navController)
                            }

                            // MUDANÇA: Adicionada a nova rota para a tela de IMC
                            composable("imc") {
                                ImcScreen(navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen(onTimeout: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val darkTheme = isSystemInDarkTheme()

    val logoRes = if (darkTheme) R.drawable.logo_escuro else R.drawable.logo_claro

    val alpha = animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000), label = ""
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(2000)
        onTimeout()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = "Logo do App",
                modifier = Modifier
                    .size(150.dp)
                    .alpha(alpha.value)
            )
        }
    }
}