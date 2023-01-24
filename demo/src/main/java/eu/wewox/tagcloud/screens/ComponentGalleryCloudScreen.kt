package eu.wewox.tagcloud.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.wewox.tagcloud.Example
import eu.wewox.tagcloud.TagCloud
import eu.wewox.tagcloud.TagCloudState
import eu.wewox.tagcloud.math.Vector3
import eu.wewox.tagcloud.rememberTagCloudState
import eu.wewox.tagcloud.ui.components.TopBar
import eu.wewox.tagcloud.ui.theme.SpacingSmall
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Showcases that every item in tag cloud is basically a Composable
 */
@Composable
fun ComponentGalleryCloudScreen() {
    Scaffold(
        topBar = { TopBar(Example.ComponentGalleryCloud.label) },
    ) { padding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            var autoRotation by remember { mutableStateOf(true) }

            val state = rememberTagCloudState(
                onStartGesture = { autoRotation = false },
                onEndGesture = { autoRotation = true },
            )

            LaunchedEffect(state, autoRotation) {
                while (isActive && autoRotation) {
                    delay(10)
                    state.rotateBy(0.001f, Vector3(1f, 1f, 1f))
                }
            }

            ComponentGalleryCloud(state)
        }
    }
}

@Composable
private fun ComponentGalleryCloud(state: TagCloudState) {
    TagCloud(
        state = state,
        modifier = Modifier.padding(64.dp)
    ) {
        items(Components) {
            Box(
                modifier = Modifier
                    .tagCloudItemFade()
                    .tagCloudItemScaleDown()
                    .padding(SpacingSmall)
            ) {
                it.invoke()
            }
        }
    }
}

private val Components = listOf<@Composable () -> Unit>(
    {
        var value by remember { mutableStateOf("") }
        TextField(
            value = value,
            onValueChange = { value = it },
            modifier = Modifier.width(120.dp)
        )
    },
    {
        var checked by remember { mutableStateOf(false) }
        Switch(
            checked = checked,
            onCheckedChange = { checked = it }
        )
    },
    {
        var checked by remember { mutableStateOf(false) }
        Checkbox(
            checked = checked,
            onCheckedChange = { checked = it }
        )
    },
    {
        var selected by remember { mutableStateOf(false) }
        FilterChip(
            selected = selected,
            onClick = { selected = !selected },
            label = { Text("Tag cloud") },
            leadingIcon = {
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null
                    )
                }
            }
        )
    },
    {
        var type by remember { mutableStateOf(0) }

        when (type) {
            0 -> Button(onClick = { type = 1 }) {
                Text("Button")
            }

            1 -> OutlinedButton(onClick = { type = 2 }) {
                Text("Outlined")
            }

            2 -> TextButton(onClick = { type = 0 }) {
                Text("Text")
            }
        }
    },
    {
        var icon by remember { mutableStateOf(Icons.Default.Check) }
        FloatingActionButton(
            onClick = {
                icon = if (icon != Icons.Default.Check) {
                    Icons.Default.Check
                } else {
                    Icons.Default.Close
                }
            }
        ) {
            AnimatedContent(targetState = icon) { target ->
                Icon(
                    imageVector = target,
                    contentDescription = null
                )
            }
        }
    },
    {
        var value by remember { mutableStateOf(0f) }
        Slider(
            value = value,
            onValueChange = { value = it },
            modifier = Modifier.width(120.dp)
        )
    },
)
