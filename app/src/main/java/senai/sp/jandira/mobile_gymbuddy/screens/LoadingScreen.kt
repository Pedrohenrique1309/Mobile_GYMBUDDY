package senai.sp.jandira.mobile_gymbuddy.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import senai.sp.jandira.mobile_gymbuddy.R

@Composable
fun LoadingScreen() {
    val darkTheme = isSystemInDarkTheme() // verifica se o sistema está no modo escuro

    // Escolhe a logo de acordo com o tema
    val logoRes = if (darkTheme) R.drawable.logo_escuro else R.drawable.logo_claro

    // Fundo de acordo com o tema
    val backgroundColor = if (darkTheme) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.background

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = "Logo do App",
                modifier = Modifier.size(150.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreviewLight() {
    LoadingScreen()
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoadingScreenPreviewDark() {
    LoadingScreen()
}
