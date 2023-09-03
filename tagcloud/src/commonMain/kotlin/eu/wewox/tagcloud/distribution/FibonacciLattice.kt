package eu.wewox.tagcloud.distribution

import eu.wewox.tagcloud.math.Vector3
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A class to generate a points uniformly distributed on a unit sphere.
 *
 * Uses the simplest method from:
 * http://extremelearning.com.au/evenly-distributing-points-on-a-sphere/
 */
public class FibonacciLattice(private val size: Int) {

    private val thetaPart: Float = PI.toFloat() * (1 + sqrt(5f))

    /**
     * Generate an item position with a given [index].
     *
     * @param index The item index.
     * @return The position on the unit sphere as [Vector3].
     */
    public fun item(index: Int): Vector3 {
        val k = index + 0.5f

        val phi = acos(1f - 2f * k / size)
        val theta = thetaPart * k

        return Vector3(
            x = cos(theta) * sin(phi),
            y = sin(theta) * sin(phi),
            z = cos(phi),
        )
    }
}
