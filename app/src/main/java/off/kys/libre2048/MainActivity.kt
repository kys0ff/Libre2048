package off.kys.libre2048

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cafe.adriel.voyager.navigator.Navigator
import off.kys.libre2048.ui.main.MainScreen
import off.kys.libre2048.ui.theme.Libre2048Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Libre2048Theme {
                Navigator(MainScreen())
            }
        }
    }
}