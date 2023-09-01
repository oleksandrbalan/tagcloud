package eu.wewox.tagcloud

import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.semantics.Role
import eu.wewox.tagcloud.math.Vector3

/**
 * The scope of the TagCloud item's content.
 */
public interface TagCloudItemScope {

    /**
     * Current position of the item in the TagCloud.
     * Could be used to alter it's appearance.
     */
    public val coordinates: Vector3

    /**
     * Fades far most items up to the given alpha.
     *
     * @param toAlpha The alpha for most items (with lowest z coordinate).
     */
    public fun Modifier.tagCloudItemFade(toAlpha: Float = 0.25f): Modifier

    /**
     * Scales down far most items up to the given scale.
     *
     * @param toScale The scale for most items (with lowest z coordinate).
     */
    public fun Modifier.tagCloudItemScaleDown(toScale: Float = 0.5f): Modifier

    public fun Modifier.tagCloudItemClickable(
        interactionSource: MutableInteractionSource,
        indication: Indication?,
        enabled: Boolean = true,
        onClickLabel: String? = null,
        role: Role? = null,
        onClick: () -> Unit
    ): Modifier

    public fun Modifier.tagCloudItemClickable(
        enabled: Boolean = true,
        onClickLabel: String? = null,
        role: Role? = null,
        onClick: () -> Unit
    ): Modifier
}

/**
 * Implementation of the [TagCloudItemScope].
 */
internal class TagCloudItemScopeImpl(
    override val coordinates: Vector3,
) : TagCloudItemScope {

    override fun Modifier.tagCloudItemFade(toAlpha: Float): Modifier =
        graphicsLayer {
            alpha = coordinates.z.rescale(toAlpha, 1f)
        }

    override fun Modifier.tagCloudItemScaleDown(toScale: Float): Modifier =
        graphicsLayer {
            val scale = coordinates.z.rescale(toScale, 1f)
            scaleX = scale
            scaleY = scale
        }

    override fun Modifier.tagCloudItemClickable(
        interactionSource: MutableInteractionSource,
        indication: Indication?,
        enabled: Boolean,
        onClickLabel: String?,
        role: Role?,
        onClick: () -> Unit
    ): Modifier = tagCloudItemPointerInputClickable(enabled) { onClick() }
        .clickable(interactionSource, indication, enabled, onClickLabel, role) {}

    override fun Modifier.tagCloudItemClickable(
        enabled: Boolean,
        onClickLabel: String?,
        role: Role?,
        onClick: () -> Unit
    ): Modifier = tagCloudItemPointerInputClickable(enabled) { onClick() }
        .clickable(enabled, onClickLabel, role) {}

    private fun Modifier.tagCloudItemPointerInputClickable(
        enabled: Boolean,
        onClick: () -> Unit
    ) = composed {
        val touchSlop = LocalViewConfiguration.current.touchSlop
        var currentPointerPosition = remember { Offset.Unspecified }
        var pointerMoveDistance = remember { 0f }
        val modifier = pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val pointer = event.changes.last()
                    when (event.type) {
                        PointerEventType.Press -> {
                            currentPointerPosition = pointer.position
                            pointerMoveDistance = 0f
                        }

                        PointerEventType.Move -> {
                            val newPos = pointer.position
                            val diff = currentPointerPosition - newPos
                            pointerMoveDistance += diff.getDistance()
                            currentPointerPosition = newPos
                        }

                        PointerEventType.Release -> {
                            if (pointerMoveDistance < touchSlop && enabled) {
                                onClick()
                            }
                        }
                    }
                }
            }
        }

        modifier
    }

}

private fun Float.rescale(newMin: Float, newMax: Float): Float =
    (((this + 1f) * (newMax - newMin)) / 2f) + newMin
