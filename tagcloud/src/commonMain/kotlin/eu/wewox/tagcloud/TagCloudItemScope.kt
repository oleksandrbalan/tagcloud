package eu.wewox.tagcloud

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import eu.wewox.tagcloud.math.Vector3

/**
 * The scope of the TagCloud item's content.
 */
public interface TagCloudItemScope {

    /**
     * Original position of the item in the TagCloud before applying any rotation.
     */
    public val originalCoordinates: Vector3

    /**
     * Current position of the item in the TagCloud.
     * Could be used to alter its appearance.
     */
    public val coordinates: Vector3

    /**
     * Fades far most items up to the given alpha.
     *
     * @param toAlpha The alpha for most items (with the lowest z coordinate).
     */
    public fun Modifier.tagCloudItemFade(toAlpha: Float = 0.25f): Modifier

    /**
     * Scales down far most items up to the given scale.
     *
     * @param toScale The scale for most items (with the lowest z coordinate).
     */
    public fun Modifier.tagCloudItemScaleDown(toScale: Float = 0.5f): Modifier

    /**
     * Fades and scales down far most items up to the given scale.
     *
     * @param toAlpha The alpha for most items (with the lowest z coordinate).
     * @param toScale The scale for most items (with the lowest z coordinate).
     */
    public fun Modifier.tagCloudItemStyle(
        toAlpha: Float = 0.25f,
        toScale: Float = 0.5f,
    ): Modifier
}

/**
 * Implementation of the [TagCloudItemScope].
 *
 * @property originalCoordinates Original position of the item in the TagCloud before applying any rotation.
 * @property currentCoordinatesProvider Provides the rotated coordinate of the item.
 */
internal class TagCloudItemScopeImpl(
    override val originalCoordinates: Vector3,
    private val currentCoordinatesProvider: () -> Vector3,
) : TagCloudItemScope {

    override val coordinates: Vector3 get() = currentCoordinatesProvider.invoke()

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

    override fun Modifier.tagCloudItemStyle(toAlpha: Float, toScale: Float): Modifier =
        graphicsLayer {
            alpha = coordinates.z.rescale(toAlpha, 1f)

            val scale = coordinates.z.rescale(toScale, 1f)
            scaleX = scale
            scaleY = scale
        }
}

private fun Float.rescale(newMin: Float, newMax: Float): Float =
    (((this + 1f) * (newMax - newMin)) / 2f) + newMin
