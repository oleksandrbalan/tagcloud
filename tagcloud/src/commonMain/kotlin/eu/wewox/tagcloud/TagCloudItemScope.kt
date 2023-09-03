package eu.wewox.tagcloud

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
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
}

private fun Float.rescale(newMin: Float, newMax: Float): Float =
    (((this + 1f) * (newMax - newMin)) / 2f) + newMin
