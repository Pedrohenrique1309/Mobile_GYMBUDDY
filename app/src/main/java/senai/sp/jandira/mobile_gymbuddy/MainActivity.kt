package senai.sp.jandira.mobile_gymbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import senai.sp.jandira.mobile_gymbuddy.screens.AppNavigation
import senai.sp.jandira.mobile_gymbuddy.ui.theme.MobileGYMBUDDYTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MobileGYMBUDDYTheme {
                AppNavigation()
            }
        }
    }
}
