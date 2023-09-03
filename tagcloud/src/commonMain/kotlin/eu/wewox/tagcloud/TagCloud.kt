package eu.wewox.tagcloud

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import eu.wewox.tagcloud.math.Quaternion
import eu.wewox.tagcloud.math.Vector3
import eu.wewox.tagcloud.math.rotate
import kotlin.math.sqrt

/**
 * The composable which displays the content on the 3D sphere, aka tag cloud.
 * In TagCloud coordinate system:
 * - "y" axis is a horizontal one with "+1" to the right and "-1" to the left.
 * - "y" axis is a vertical one with "+1" on top and "-1" at the bottom.
 * - "z" axis is like a z-index with "+1" on above and "-1" below.
 *
 * @param state The state of the TagCloud, used to observe and change it's rotation.
 * @param modifier The modifier for the root composable.
 * @param content The content lambda to register items to be shown in the TagCloud.
 */
@Composable
public fun TagCloud(
    state: TagCloudState,
    modifier: Modifier = Modifier,
    content: TagCloudScope.() -> Unit,
) {
    // Cache a radius, as it is needed to correctly calculate rotation gesture
    var radius by remember { mutableStateOf(0) }

    // Invoke content lambda to get items and rotate them based on the current rotation state
    val latestContent = rememberUpdatedState(content)
    val staticItems = remember { TagCloudScopeImpl().apply(latestContent.value).items }
    val items = staticItems.map { it.copy(coordinates = it.coordinates.rotate(state.rotation)) }

    Layout(
        content = {
            items.forEach { item ->
                val scope = TagCloudItemScopeImpl(item.coordinates)
                item.content.invoke(scope)
            }
        },
        modifier = modifier.rotateGesture(state) { radius }
    ) { measurables, constraints ->
        // Recalculate radius and update if needed
        val newRadius = minOf(constraints.maxWidth, constraints.maxHeight) / 2
        if (newRadius != radius) {
            radius = newRadius
        }

        // Measure TagCloud items
        val loosedConstraints = Constraints()
        val placeables = measurables.map { it.measure(loosedConstraints) }

        layout(radius * 2, radius * 2) {
            // Place TagCloud items, check getItemOffset() method
            placeables.forEachIndexed { index, placeable ->
                val coordinates = items[index].coordinates
                val offset = placeable.getItemOffset(coordinates, radius)
                placeable.place(offset, zIndex = coordinates.z)
            }
        }
    }
}

/**
 * Rotates a TagCloud by calculating two vectors of the drag gesture (previous and current position)
 * and rotates a TagCloud by an angle between the two.
 *
 * @param state The state of the TagCloud to change it's rotation.
 * @param radiusProvider The lambda which returns the current radius of the TagCloud.
 */
private fun Modifier.rotateGesture(
    state: TagCloudState,
    radiusProvider: () -> Int,
): Modifier = rotateGesture(
    key = state,
    enabled = state.gestureEnabled,
    onStart = state.onStartGesture,
    onEnd = state.onEndGesture,
) { from, to ->
    val radius = radiusProvider()
    // Transforms the given 2D offsets to the 3D coordinates (vectors) on the TagCloud surface
    val fromVector = from.getSphereCoordinates(radius)
    val toVector = to.getSphereCoordinates(radius)

    // Calculate a rotation quaternion between the two vectors
    val quaternion = Quaternion.create(fromVector, toVector)

    // Perform rotation by the calculated quaternion
    state.rotateBy(quaternion)
}

/**
 * Calculates the offset of the item to be placed based on it's coordinates in the TagCloud.
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
