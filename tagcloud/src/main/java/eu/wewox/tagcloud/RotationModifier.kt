package eu.wewox.tagcloud

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

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
): Modifier = pointerInput(enabled, key) {
    if (!enabled) {
        return@pointerInput
    }
    forEachGesture {
        awaitPointerEventScope {
            val down = awaitFirstDown(requireUnconsumed = false)
            onStart()
            var dragCurrent = down.position
            drag(down.id) { change ->
                onRotate(dragCurrent, change.position)
                dragCurrent = change.position
                change.consume()
            }
            onEnd()
        }
    }
}
