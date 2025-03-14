package tbax.gamedev.letsroll.letsroll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import tbax.gamedev.letsroll.letsroll.ui.LetsRollApp
import tbax.gamedev.letsroll.letsroll.ui.theme.LetsRollTheme
import tbax.gamedev.letsroll.letsroll.ui.theme.backgroundColor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LetsRollTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = backgroundColor
                ) {
                   LetsRollApp()
                }
            }
        }
    }
}