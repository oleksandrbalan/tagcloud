package eu.wewox.tagcloud.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun SimpleTagCloudScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopBar(
                title = Example.SimpleTagCloud.label,
                onBackClick = onBackClick
            )
        },
    ) { padding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            var item by remember { mutableStateOf<String?>(null) }

            SimpleTagCloud(
                onItemSelected = { item = it }
            )

            if (item != null) {
                Snackbar(
                    action = {
                        TextButton(
                            onClick = {
                                item = null
                            }
                        ) {
                            Text(text = "Close")
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(SpacingSmall)
                ) {
                    Text(text = "Item selected: $item")
                }
            }
        }
    }
}

@Composable
private fun SimpleTagCloud(onItemSelected: (String) -> Unit) {
    val labels = List(32) { "Item #$it" }

    TagCloud(
        state = rememberTagCloudState(),
        modifier = Modifier.padding(64.dp)
    ) {
        items(labels) {
            Surface(
                shape = RoundedCornerShape(SpacingSmall),
                color = MaterialTheme.colorScheme.primary,
                onClick = { onItemSelected(it) },
                modifier = Modifier
                    .tagCloudItemFade()
                    .tagCloudItemScaleDown()
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(SpacingSmall)
                )
            }
        }
    }
}
