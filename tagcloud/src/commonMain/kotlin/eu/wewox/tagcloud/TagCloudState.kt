package eu.wewox.tagcloud

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import eu.wewox.tagcloud.math.Quaternion
import eu.wewox.tagcloud.math.Vector3

/**
 * Creates a [TagCloudState] with the default properties and memorizes it.
 *
 * @param gestureEnabled True if gesture rotation is enabled, false otherwise.
 * @param rotation The initial rotation of the TagCloud.
 * @param onStartGesture The lambda which is invoked when rotation gesture has started.
 * @param onEndGesture The lambda which is invoked when rotation gesture has ended.
 */
@Composable
public fun rememberTagCloudState(
    gestureEnabled: Boolean = true,
    rotation: Quaternion = Quaternion.Identity,
    onStartGesture: () -> Unit = {},
    onEndGesture: () -> Unit = {},
): TagCloudState = rememberSaveable(
    saver = listSaver(
        save = {
            listOf(
                it.gestureEnabled,
                it.rotation.w,
                it.rotation.x,
                it.rotation.y,
                it.rotation.z
            )
        },
        restore = {
            TagCloudState(
                gestureEnabled = it[0] as Boolean,
                rotation = Quaternion(
                    w = it[1] as Float,
                    x = it[2] as Float,
                    y = it[3] as Float,
                    z = it[4] as Float,
                ),
                onStartGesture = onStartGesture,
                onEndGesture = onEndGesture,
            )
        }
    )
) {
    TagCloudState(
        gestureEnabled = gestureEnabled,
        rotation = rotation,
        onStartGesture = onStartGesture,
        onEndGesture = onEndGesture
    )
}

/**
 * The state of the [TagCloud].
 *
 * @param gestureEnabled True if gesture rotation is enabled, false otherwise.
 * @param rotation The initial rotation of the TagCloud.
 * @property onStartGesture The lambda which is invoked when rotation gesture has started.
 * @property onEndGesture The lambda which is invoked when rotation gesture has ended.
 */
public class TagCloudState(
    gestureEnabled: Boolean,
    rotation: Quaternion,
    internal val onStartGesture: () -> Unit,
    internal val onEndGesture: () -> Unit,
) {

    /**
     * True if gesture rotation is enabled, false otherwise.
     */
    public var gestureEnabled: Boolean by mutableStateOf(gestureEnabled)

    /**
     * The current rotation of the TagCloud.
     */
    public var rotation: Quaternion by mutableStateOf(rotation)
        private set

    /**
     * Rotates a TagCloud by the given [angle] along the [vector].
     *
     * @param angle The angle to rotate by.
     * @param vector The vector to rotate along.
     * @param globalAxis True if vector is used in global axis, false if rotate using local
     * TagCloud axis.
     */
    public fun rotateBy(angle: Float, vector: Vector3, globalAxis: Boolean = true) {
        val quaternion = Quaternion.create(angle, vector)
        rotateBy(quaternion, globalAxis)
    }

    /**
     * Rotates a TagCloud by the given rotation [quaternion].
     *
     * @param quaternion The quaternion to rotate by.
     * @param globalAxis True if vector is used in global axis, false if rotate using local
     * TagCloud axis.
     */
    public fun rotateBy(quaternion: Quaternion, globalAxis: Boolean = true) {
        rotateTo(if (globalAxis) quaternion * rotation else rotation * quaternion)
    }

    /**
     * Rotates a TagCloud to the given [angle] along the [vector].
     *
     * @param angle The angle to rotate.
     * @param vector The vector to rotate.
     */
    public fun rotateTo(angle: Float, vector: Vector3) {
        val quaternion = Quaternion.create(angle, vector)
        rotateTo(quaternion)
    }

    /**
     * Rotates a TagCloud to the given rotation [quaternion].
     *
     * @param quaternion The quaternion to rotate.
     */
    public fun rotateTo(quaternion: Quaternion) {
        rotation = quaternion
    }
}
