package eu.wewox.tagcloud.math

import kotlin.math.sqrt

/**
 * Simple 3D vector class with [x], [y] and [z] coordinates.
 */
public data class Vector3(
    val x: Float,
    val y: Float,
    val z: Float,
) {

    /**
     * Creates a new normalized 3D vector.
     *
     * @return The new 3D unit vector.
     */
    public fun normalized(): Vector3 {
        if (this == Zero) return this
        val norm = 1f / sqrt(dotProduct(this, this))
        return this * norm
    }

    /**
     * Multiplies the vector by the given value.
     *
     * @param value The value to multiply a vector.
     * @return The multiplication result.
     */
    public operator fun times(value: Float): Vector3 =
        Vector3(x * value, y * value, z * value)

    public companion object {

        /**
         * Zero 3D vector.
         */
        public val Zero: Vector3 = Vector3(0f, 0f, 0f)

        /**
         * Calculates a cross product of two vectors.
         *
         * @param l First vector.
         * @param r Second vector.
         * @return The resulted vector.
         */
        public fun crossProduct(l: Vector3, r: Vector3): Vector3 =
            Vector3(
                x = l.y * r.z - l.z * r.y,
                y = l.z * r.x - l.x * r.z,
                z = l.x * r.y - l.y * r.x,
            )

        /**
         * Calculates a dot product of two vectors.
         *
         * @param l First vector.
         * @param r Second vector.
         * @return The resulted value.
         */
        public fun dotProduct(l: Vector3, r: Vector3): Float =
            l.x * r.x + l.y * r.y + l.z * r.z
    }
}
