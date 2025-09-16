package senai.sp.jandira.mobile_gymbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import senai.sp.jandira.mobile_gymbuddy.screens.LoginScreen
import senai.sp.jandira.mobile_gymbuddy.ui.theme.MobileGYMBUDDYTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileGYMBUDDYTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    // Exibe a Splash Screen temporária
                    LoadingScreen(onTimeout = { showSplash = false })
                } else {
                    // Depois da Splash, mostra a tela de login
                    LoginScreen()
                }
            }
        }
    }
}

// ------------------- Composable da Splash Screen -------------------
@Composable
fun LoadingScreen(onTimeout: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val darkTheme = isSystemInDarkTheme()

    // Seleciona a logo de acordo com o tema
    val logoRes = if (darkTheme) R.drawable.logo_escuro else R.drawable.logo_claro

    // Animação de fade-in da logo
    val alpha = animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    // Executa a animação e o tempo da Splash
    LaunchedEffect(Unit) {
        visible = true
        delay(2000) // duração da splash em milissegundos
        onTimeout()  // chama a função que troca para LoginScreen
    }

    // Tela da Splash
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
