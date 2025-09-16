package senai.sp.jandira.mobile_gymbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import senai.sp.jandira.mobile_gymbuddy.ui.theme.MobileGYMBUDDYTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileGYMBUDDYTheme {
                LoadingScreen()
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    var visible by remember { mutableStateOf(false) }
    val darkTheme = isSystemInDarkTheme()

    // Escolhe a logo de acordo com o tema
    val logoRes = if (darkTheme) R.drawable.logo_escuro else R.drawable.logo_claro

    // Animação de fade-in
    val alpha = animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = if (darkTheme) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = "Logo do App",
                modifier = Modifier
                    .size(250.dp)
                    .alpha(alpha.value)
            )
        }
    }
}
