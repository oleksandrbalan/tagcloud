package eu.wewox.tagcloud.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.wewox.tagcloud.Example
import eu.wewox.tagcloud.TagCloud
import eu.wewox.tagcloud.TagCloudItemScope
import eu.wewox.tagcloud.TagCloudState
import eu.wewox.tagcloud.math.Vector3
import eu.wewox.tagcloud.rememberTagCloudState
import eu.wewox.tagcloud.ui.components.TopBar
import eu.wewox.tagcloud.ui.theme.SpacingMedium
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sign

/**
 * Example of how to use state.
 */
@Composable
fun StatInTagCloudScreen() {
    Scaffold(
        topBar = { TopBar(Example.StateInTagCloud.label) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val autoRotation = remember { AutoRotation() }

            val state = rememberTagCloudState(
                onStartGesture = { autoRotation.enabled = false },
                onEndGesture = { autoRotation.enabled = true },
            )

            LaunchedEffect(state, autoRotation.enabled) {
                while (isActive && autoRotation.enabled) {
                    delay(DelayPerFrame)
                    val vector = autoRotation.vector
                    if (vector != Vector3.Zero) {
                        state.rotateBy(autoRotation.angle, vector)
                    }
                }
            }

            TagCloudAxis(
                state = state,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterHorizontally)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingMedium)
            ) {
                Text(text = "Gesture enabled")
                Switch(
                    checked = state.gestureEnabled,
                    onCheckedChange = { state.gestureEnabled = it }
                )

                autoRotation.Controls(
                    onResetClick = { state.rotateTo(0f, Vector3.Zero) }
                )
            }
        }
    }
}

@Composable
private fun TagCloudAxis(
    state: TagCloudState,
    modifier: Modifier
) {
    TagCloud(state, modifier.padding(32.dp)) {
        val size = 3
        val values = (-size..size step 1).map { it / size.toFloat() } - 0f

        items(values.filter { it > 0 }.map { Vector3(it, 0f, 0f) }) {
            AxisItem("+x", Color.Red)
        }
        items(values.filter { it < 0 }.map { Vector3(it, 0f, 0f) }) {
            AxisItem("-x", Color.Black)
        }
        items(values.filter { it > 0 }.map { Vector3(0f, it, 0f) }) {
            AxisItem("+y", Color.Green)
        }
        items(values.filter { it < 0 }.map { Vector3(0f, it, 0f) }) {
            AxisItem("-y", Color.Black)
        }
        items(values.filter { it > 0 }.map { Vector3(0f, 0f, it) }) {
            AxisItem("+z", Color.Blue)
        }
        items(values.filter { it < 0 }.map { Vector3(0f, 0f, it) }) {
            AxisItem("-z", Color.Black)
        }
    }
}

@Composable
private fun TagCloudItemScope.AxisItem(text: String, color: Color) {
    Surface(
        color = Color.Transparent,
        contentColor = color,
        modifier = Modifier
            .tagCloudItemFade()
            .tagCloudItemScaleDown()
    ) {
        Column {
            Text(text = text, fontSize = 32.sp)
            val sign = if (coordinates.z.sign < 0) "-" else "+"
            Text(text = String.format(Locale.ENGLISH, "z: %s%.02f", sign, abs(coordinates.z)))
        }
    }
}

private class AutoRotation(duration: Long = 16_000L) {
    private var xPos by mutableStateOf(false)
    private var xNeg by mutableStateOf(false)
    private var yPos by mutableStateOf(true)
    private var yNeg by mutableStateOf(false)
    private var zPos by mutableStateOf(false)
    private var zNeg by mutableStateOf(false)

    var enabled by mutableStateOf(true)

    val angle: Float = 2f * PI.toFloat() / duration * DelayPerFrame

    val vector: Vector3
        get() = Vector3(
            if (xPos) 1f else if (xNeg) -1f else 0f,
            if (yPos) 1f else if (yNeg) -1f else 0f,
            if (zPos) 1f else if (zNeg) -1f else 0f,
        )

    @Composable
    fun Controls(onResetClick: () -> Unit) {
        Text(text = "Auto rotation")
        Button(onClick = onResetClick) { Text(text = "Reset rotation") }
        ControlRow("x", xNeg, xPos, { xNeg = it }, { xPos = it })
        ControlRow("y", yNeg, yPos, { yNeg = it }, { yPos = it })
        ControlRow("z", zNeg, zPos, { zNeg = it }, { zPos = it })
    }

    @Composable
    private fun ControlRow(
        text: String,
        negative: Boolean,
        positive: Boolean,
        setNegative: (Boolean) -> Unit,
        setPositive: (Boolean) -> Unit,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Switch(
                checked = negative,
                onCheckedChange = { setNegative(it); setPositive(false) }
            )
            Text(text = text)
            Switch(
                checked = positive,
                onCheckedChange = { setPositive(it); setNegative(false) }
            )
        }
    }
}

private const val DelayPerFrame: Long = 10L
