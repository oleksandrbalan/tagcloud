package eu.wewox.tagcloud

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import eu.wewox.tagcloud.math.Quaternion
import eu.wewox.tagcloud.math.Vector3
import eu.wewox.tagcloud.math.rotate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * The composable which displays the content on the 3D sphere, aka tag cloud.
 * In TagCloud coordinate system:
 * - "x" axis is a horizontal one with "+1" to the right and "-1" to the left.
 * - "y" axis is a vertical one with "+1" on top and "-1" at the bottom.
 * - "z" axis is like a z-index with "+1" on above and "-1" below.
 *
 * @param state The state of the TagCloud, used to observe and change its rotation.
 * @param modifier The modifier for the root composable.
 * @param contentPadding The padding around the whole content.
 * @param content The content lambda to register items to be shown in the TagCloud.
 */
@Composable
public fun TagCloud(
    state: TagCloudState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: TagCloudScope.() -> Unit,
) {
    // Scope for internal animations
    val coroutineScope = rememberCoroutineScope()

    // Cache layout info, as it is needed to correctly calculate rotation gesture
    var layoutInfo by remember { mutableStateOf(TagCloudLayoutInfo()) }

    // Invoke content lambda to get items and rotate them based on the current rotation state
    val latestContent = rememberUpdatedState(content)
    val staticItems = remember { TagCloudScopeImpl().apply(latestContent.value).items }

    Layout(
        content = {
            staticItems.forEach { item ->
                val scope = TagCloudItemScopeImpl(
                    originalCoordinates = item.coordinates,
                    currentCoordinatesProvider = {
                        item.coordinates.rotate(state.rotation)
                    }
                )
                item.content.invoke(scope)
            }
        },
        modifier = modifier.rotateGesture(
            state = state,
            coroutineScope = coroutineScope,
            layoutInfoProvider = { layoutInfo }
        )
    ) { measurables, constraints ->
        val startPadding = contentPadding.calculateStartPadding(layoutDirection).roundToPx()
        val topPadding = contentPadding.calculateTopPadding().roundToPx()
        val endPadding = contentPadding.calculateEndPadding(layoutDirection).roundToPx()
        val bottomPadding = contentPadding.calculateBottomPadding().roundToPx()

        val availableWidth = constraints.maxWidth - startPadding - endPadding
        val availableHeight = constraints.maxHeight - topPadding - bottomPadding
        // Recalculate radius and update if needed
        val newRadius = minOf(availableWidth, availableHeight) / 2
        if (newRadius != layoutInfo.radius || startPadding != layoutInfo.contentOffsetX || topPadding != layoutInfo.contentOffsetY) {
            layoutInfo = TagCloudLayoutInfo(newRadius, startPadding, topPadding)
        }

        // Measure TagCloud items
        val loosedConstraints = Constraints()
        val placeables = measurables.map { it.measure(loosedConstraints) }

        layout(
            width = newRadius * 2 + startPadding + endPadding,
            height = newRadius * 2 + topPadding + bottomPadding,
        ) {
            // Place TagCloud items, check getItemOffset() method
            placeables.forEachIndexed { index, placeable ->
                val coordinates = staticItems[index].coordinates.rotate(state.rotation)
                val offset = placeable.getItemOffset(coordinates, newRadius)
                placeable.placeRelative(
                    x = offset.x + startPadding,
                    y = offset.y + topPadding,
                    zIndex = coordinates.z,
                )
            }
        }
    }
}

/**
 * Rotates a TagCloud by calculating two vectors of the drag gesture (previous and current position)
 * and rotates a TagCloud by an angle between the two.
 *
 * @param state The state of the TagCloud to change its rotation.
 * @param coroutineScope The scope for internal animations.
 * @param layoutInfoProvider The lambda which returns the current layout info of the TagCloud.
 */
private fun Modifier.rotateGesture(
    state: TagCloudState,
    coroutineScope: CoroutineScope,
    layoutInfoProvider: () -> TagCloudLayoutInfo,
): Modifier = rotateGesture(
    key = state,
    enabled = state.gestureEnabled,
    onStart = state.onStartGesture,
    onEnd = state.onEndGesture,
) { from, to ->
    val info = layoutInfoProvider()
    // Transforms the given 2D offsets to the 3D coordinates (vectors) on the TagCloud surface
    val fromVector = from.minus(info.contentOffset).getSphereCoordinates(info.radius)
    val toVector = to.minus(info.contentOffset).getSphereCoordinates(info.radius)

    // Calculate a rotation quaternion between the two vectors
    val quaternion = Quaternion.create(fromVector, toVector)

    // Perform rotation by the calculated quaternion
    coroutineScope.launch {
        state.rotateBy(quaternion)
    }
}

/**
 * Calculates the offset of the item to be placed based on its coordinates in the TagCloud.
 *
 * @param coordinates The item position in the TagCloud.
 * @param radius The radius of the TagCloud.
 * @return Offset where item should be placed.
 */
private fun Placeable.getItemOffset(
    coordinates: Vector3,
    radius: Int,
): IntOffset {
    val x = radius * (1 + coordinates.x) - width / 2
    val y = radius * (1 - coordinates.y) - height / 2
    return IntOffset(x.toInt(), y.toInt())
}

/**
 * Calculates the position on the TagCloud based on the 2D offset.
 *
 * @param radius The radius of the TagCloud.
 * @return The position on the TagCloud.
 */
private fun Offset.getSphereCoordinates(
    radius: Int,
): Vector3 {
    val dx = (x - radius) / radius
    val dy = (y - radius) / radius * (-1)
    val dz = sqrt(1 - dx * dx - dy * dy).takeUnless { it.isNaN() } ?: 0f
    return Vector3(dx, dy, dz).normalized()
}

private data class TagCloudLayoutInfo(
    val radius: Int = 0,
    val contentOffsetX: Int = 0,
    val contentOffsetY: Int = 0,
) {
    val contentOffset: Offset = IntOffset(contentOffsetX, contentOffsetY).toOffset()
}
