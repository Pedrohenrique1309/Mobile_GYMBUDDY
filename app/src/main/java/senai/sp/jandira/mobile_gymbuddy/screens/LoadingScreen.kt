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
import senai.sp.jandira.mobile_gymbuddy.R

@Composable
fun LoadingScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // ou Color.Black para fundo totalmente escuro
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_escuro), // sua logo
                contentDescription = "Logo do App",
                modifier = Modifier.size(150.dp)
            )
        }
    }
}

// Preview para visualizar no Android Studio
@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    LoadingScreen()
}
