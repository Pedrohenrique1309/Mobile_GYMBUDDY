package senai.sp.jandira.mobile_gymbuddy.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf(AppScreen.Login) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentScreen) {
            AppScreen.Login -> {
                LoginScreen(
                    onNavigateToRegister = { currentScreen = AppScreen.Cadastro },
                    onNavigateToRecovery = { currentScreen = AppScreen.RecuperacaoSenha },
                    onLoginSuccess = { currentScreen = AppScreen.PrimeirosPassos }
                )
            }
            AppScreen.Cadastro -> {
                CadastroScreen(
                    onNavigateToLogin = { currentScreen = AppScreen.Login },
                    onCadastroSuccess = { currentScreen = AppScreen.Login }
                )
            }
            AppScreen.RecuperacaoSenha -> {
                RecuperacaoSenhaScreen(
                    onNavigateToLogin = { currentScreen = AppScreen.Login },
                    onEnviarSenha = { currentScreen = AppScreen.Login }
                )
            }
            AppScreen.PrimeirosPassos -> {
                PrimeirosPassosScreen(
                    onComecarClick = { 
                        // Aqui você pode navegar para próximas telas do app
                        // Por enquanto, volta para login como demonstração
                        currentScreen = AppScreen.Login 
                    },
                    onLogout = { currentScreen = AppScreen.Login }
                )
            }
        }
    }
}

enum class AppScreen {
    Login,
    Cadastro,
    RecuperacaoSenha,
    PrimeirosPassos
}
