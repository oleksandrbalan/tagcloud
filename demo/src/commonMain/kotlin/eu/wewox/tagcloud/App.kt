package eu.wewox.tagcloud

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import eu.wewox.tagcloud.screens.ComponentGalleryCloudScreen
import eu.wewox.tagcloud.screens.FeaturesCloudScreen
import eu.wewox.tagcloud.screens.SimpleTagCloudScreen
import eu.wewox.tagcloud.screens.StatInTagCloudScreen
import eu.wewox.tagcloud.ui.theme.TagCloudTheme

@Composable
fun App(modifier: Modifier = Modifier) {
    var example by rememberSaveable { mutableStateOf<Example?>(null) }
    App(
        example = example,
        onChangeExample = { example = it },
        modifier = modifier,
    )
}

@Composable
fun App(
    example: Example?,
    onChangeExample: (Example?) -> Unit,
    modifier: Modifier = Modifier,
) {
    TagCloudTheme {
        val reset = { onChangeExample(null) }

        Crossfade(example, modifier) { selected ->
            when (selected) {
                null -> RootScreen(onExampleClick = onChangeExample)
                Example.SimpleTagCloud -> SimpleTagCloudScreen(reset)
                Example.StateInTagCloud -> StatInTagCloudScreen(reset)
                Example.ComponentGalleryCloud -> ComponentGalleryCloudScreen(reset)
                Example.FeaturesCloud -> FeaturesCloudScreen(reset)
            }
        }
    }
}
