package eu.wewox.tagcloud

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration

/**
 * The custom modifier to detect rotation gesture on the TagCloud.
 *
 * @param key The one of keys of the [pointerInput].
 * @param enabled True when this gestures is enabled.
 * @param onStart The lambda which is invoked when gesture starts.
 * @param onEnd The lambda which is invoked when gesture ends.
 * @param onRotate The lambda which is invoked during gesture. Has two offsets as arguments:
 * previous position of the pointer and current position of the pointer.
 */
internal fun Modifier.rotateGesture(
    key: Any?,
    enabled: Boolean,
    onStart: () -> Unit,
    onEnd: () -> Unit,
    onRotate: (Offset, Offset) -> Unit,
): Modifier = composed {
    val touchSlop = LocalViewConfiguration.current.touchSlop
    pointerInput(enabled, key) {
        if (!enabled) {
            return@pointerInput
        }
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            onStart()
            var dragCurrent = down.position
            drag(down.id) { change ->
                onRotate(dragCurrent, change.position)
                dragCurrent = change.position
                if ((change.position - down.position).getDistance() > touchSlop) {
                    change.consume()
                }
            }
            onEnd()
        }
    }
}
