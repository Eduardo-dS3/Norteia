package io.github.eduardods3.norteia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import io.github.eduardods3.norteia.ui.CompassScreen
import io.github.eduardods3.norteia.ui.MainViewModel
import io.github.eduardods3.norteia.ui.theme.NorteiaTheme

/**
 * The whole app is one screen. This Activity just wires the splash, the
 * edge-to-edge window and the single [MainViewModel] into [CompassScreen].
 */
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            NorteiaTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    CompassScreen(viewModel)
                }
            }
        }
    }
}
