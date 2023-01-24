package eu.wewox.tagcloud.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.wewox.tagcloud.ui.theme.SpacingMedium

/**
 * The reusable component for top bar.
 *
 * @param title The text to show in top bar.
 */
@Composable
fun TopBar(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier
            .padding(SpacingMedium)
            .statusBarsPadding()
    )
}
