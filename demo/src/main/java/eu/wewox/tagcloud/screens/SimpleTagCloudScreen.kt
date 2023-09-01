package eu.wewox.tagcloud.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import eu.wewox.tagcloud.Example
import eu.wewox.tagcloud.TagCloud
import eu.wewox.tagcloud.rememberTagCloudState
import eu.wewox.tagcloud.ui.components.TopBar
import eu.wewox.tagcloud.ui.theme.SpacingSmall

/**
 * Basic tag cloud usage.
 */
@Composable
fun SimpleTagCloudScreen() {
    Scaffold(
        topBar = { TopBar(Example.SimpleTagCloud.label) },
    ) { padding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SimpleTagCloud()
        }
    }
}

@Composable
private fun SimpleTagCloud() {
    val labels = List(32) { "Item #$it" }
    val ctx = LocalContext.current

    TagCloud(
        state = rememberTagCloudState(),
        modifier = Modifier.padding(64.dp)
    ) {
        items(labels) {
            Surface(
                shape = RoundedCornerShape(SpacingSmall),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .tagCloudItemFade()
                    .tagCloudItemScaleDown()
                    .tagCloudItemClickable {
                        Toast.makeText(ctx, "Clicked", Toast.LENGTH_SHORT).show()
                    }
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(SpacingSmall)
                )
            }
        }
    }
}
