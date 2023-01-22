package eu.wewox.tagcloud.math

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Rotation quaternion.
 *
 * https://en.wikipedia.org/wiki/Quaternions_and_spatial_rotation
 */
public class Quaternion internal constructor(w: Float, x: Float, y: Float, z: Float) {

    /**
     * The rotation angle.
     */
    public var w: Float = w
        private set

    /**
     * The rotation "x" axis coordinate.
     */
    public var x: Float = x
        private set

    /**
     * The rotation "y" axis coordinate.
     */
    public var y: Float = y
        private set

    /**
     * The rotation "z" axis coordinate.
     */
    public var z: Float = z
        private set

    init {
        normalize()
    }

    /**
     * Rescales the quaternion to the unit length.
     */
    private fun normalize() {
        val squared = x * x + y * y + z * z + w * w
        if (abs(squared - 1f) > 1.0E-10f) {
            val norm = sqrt(squared)
            x /= norm
            y /= norm
            z /= norm
            w /= norm
        }
    }

    /**
     * Combines this quaternion with a given one.
     * The result quaternion is equivalent to performing two rotations sequentially.
     * Ordering is important for this operation.
     *
     * @param other The quaternion to combine with.
     * @return The combined rotation quaternion.
     */
    public operator fun times(other: Quaternion): Quaternion =
        Quaternion(
            w * other.w - x * other.x - y * other.y - z * other.z,
            w * other.x + x * other.w + y * other.z - z * other.y,
            w * other.y - x * other.z + y * other.w + z * other.x,
            w * other.z + x * other.y - y * other.x + z * other.w,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Quaternion

        if (w != other.w) return false
        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false

        return true
    }

    override fun hashCode(): Int {
        var result = w.hashCode()
        result = 31 * result + x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }

    public companion object {

        /**
         * Identity quaternion, means no rotation.
         */
        public val Identity: Quaternion = create(0f, Vector3(0f, 0f, 0f))

        /**
         * Creates a new quaternion based on the [angle] and [vector] to rotate around.
         */
        public fun create(angle: Float, vector: Vector3): Quaternion {
            val sin = sin(angle / 2f)
            val cos = cos(angle / 2f)
            return Quaternion(
                w = cos,
                x = vector.x * sin,
                y = vector.y * sin,
                z = vector.z * sin,
            )
        }

        /**
         * Gets the rotation quaternion which was used to rotate [from] vector to the [to] vector.
         *
         * @param from The initial vector.
         * @param to The final vector.
         * @return The rotation quaternion.
         */
        public fun create(from: Vector3, to: Vector3): Quaternion {
            val normal = Vector3.crossProduct(from, to)
            val angle = Vector3.dotProduct(from, to)
            return create(angle, normal)
        }
    }
}

/**
 * Rotates the vector with a given [quaternion].
 *
 * @param quaternion The rotation quaternion to be used for rotation.
 * @return The result vector after rotation.
 */
public fun Vector3.rotate(quaternion: Quaternion): Vector3 {
    val w2 = quaternion.w * quaternion.w
    val x2 = quaternion.x * quaternion.x
    val y2 = quaternion.y * quaternion.y
    val z2 = quaternion.z * quaternion.z
    val zw = quaternion.z * quaternion.w
    val xy = quaternion.x * quaternion.y
    val xz = quaternion.x * quaternion.z
    val yw = quaternion.y * quaternion.w
    val yz = quaternion.y * quaternion.z
    val xw = quaternion.x * quaternion.w
    val m00 = w2 + x2 - z2 - y2
    val m01 = xy + zw + zw + xy
    val m02 = xz - yw + xz - yw
    val m10 = -zw + xy - zw + xy
    val m11 = y2 - z2 + w2 - x2
    val m12 = yz + yz + xw + xw
    val m20 = yw + xz + xz + yw
    val m21 = yz + yz - xw - xw
    val m22 = z2 - y2 - x2 + w2
    return Vector3(
        x = m00 * x + m10 * y + m20 * z,
        y = m01 * x + m11 * y + m21 * z,
        z = m02 * x + m12 * y + m22 * z,
    )
}
