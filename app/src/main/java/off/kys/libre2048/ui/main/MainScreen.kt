package off.kys.libre2048.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import off.kys.libre2048.data.repository.GameRepository
import off.kys.libre2048.domain.model.GameMode
import off.kys.libre2048.ui.common.GameBoardPreview
import off.kys.libre2048.ui.game.Game2048Screen
import org.koin.compose.koinInject

class MainScreen : Screen {

    private val squareModes = listOf(
        GameMode(4, 4, "4x4"),
        GameMode(5, 5, "5x5"),
        GameMode(6, 6, "6x6"),
        GameMode(8, 8, "8x8")
    )

    private val rectModes = listOf(
        GameMode(3, 5, "3x5"),
        GameMode(4, 6, "4x6"),
        GameMode(5, 8, "5x8"),
        GameMode(6, 9, "6x9")
    )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repository = koinInject<GameRepository>()

        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = "2048",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    fontSize = 80.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "Square Modes",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 24.dp).align(Alignment.Start)
                )
                ModeRow(squareModes, repository, navigator)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Rectangular Modes",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 24.dp).align(Alignment.Start)
                )
                ModeRow(rectModes, repository, navigator)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    @Composable
    private fun ModeRow(modes: List<GameMode>, repository: GameRepository, navigator: Navigator) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(modes) { mode ->
                ModeCard(mode, repository, navigator)
            }
        }
    }

    @Composable
    private fun ModeCard(mode: GameMode, repository: GameRepository, navigator: Navigator) {
        val savedState by repository.getCurrentState(mode.rows, mode.cols).collectAsState(initial = null)

        Card(
            modifier = Modifier.width(140.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GameBoardPreview(
                    rows = mode.rows,
                    cols = mode.cols,
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(mode.label, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { navigator.push(Game2048Screen(rows = mode.rows, cols = mode.cols, resume = savedState != null)) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(if (savedState != null) "Resume" else "Play", fontSize = 12.sp)
                }
            }
        }
    }
}