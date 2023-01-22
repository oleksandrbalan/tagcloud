package eu.wewox.tagcloud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import eu.wewox.tagcloud.ui.theme.TagCloudTheme

/**
 * Main activity for demo application.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            TagCloudTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        val state = rememberTagCloudState()
                        TagCloudFibonacciLattice(state)
                    }
                }
            }
        }
    }
}

@Composable
private fun TagCloudFibonacciLattice(state: TagCloudState) {
    TagCloud(
        state = state,
        modifier = Modifier.padding(64.dp)
    ) {
        items(List(32) { "Item #$it" }) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .tagCloudItemFade()
                    .tagCloudItemScaleDown()
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
