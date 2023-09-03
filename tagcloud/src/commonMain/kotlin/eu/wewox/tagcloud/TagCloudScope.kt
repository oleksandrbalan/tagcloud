package eu.wewox.tagcloud

import androidx.compose.runtime.Composable
import eu.wewox.tagcloud.distribution.FibonacciLattice
import eu.wewox.tagcloud.math.Quaternion
import eu.wewox.tagcloud.math.Vector3
import eu.wewox.tagcloud.math.rotate

/**
 * Receiver scope which is used by [TagCloud].
 */
public interface TagCloudScope {

    /**
     * Adds a single item.
     *
     * @param coordinates The initial item position in the TagCloud (without applied rotation).
     * @param content The content of the item.
     */
    public fun item(
        coordinates: Vector3,
        content: @Composable TagCloudItemScope.() -> Unit
    )

    /**
     * Adds multiple items by their initial positions in the TagCloud.
     *
     * @param coordinates The initial items position in the TagCloud (without applied rotation).
     * @param content The content displayed by a single item.
     */
    public fun items(
        coordinates: List<Vector3>,
        content: @Composable TagCloudItemScope.(Vector3) -> Unit
    )

    /**
     * Adds multiple items by their initial positions in the TagCloud.
     *
     * @param coordinates The initial items position in the TagCloud (without applied rotation).
     * @param content The content displayed by a single item.
     */
    public fun itemsIndexed(
        coordinates: List<Vector3>,
        content: @Composable TagCloudItemScope.(Int, Vector3) -> Unit
    )

    /**
     * Adds multiple items which are uniformly distributed on the TagCloud surface.
     *
     * @param items The list of items to place.
     * @param layer The layer for each item, could be used to control how "deep" item is placed
     * in the TagCloud. By default layer is set to 1, which means the sphere surface.
     * @param rotation The initial rotation for each item.
     * @param content The content displayed by a single item.
     */
    public fun <T> items(
        items: List<T>,
        layer: (T) -> Float = { DefaultLayer },
        rotation: (T) -> Quaternion = { Quaternion.Identity },
        content: @Composable TagCloudItemScope.(T) -> Unit
    )

    /**
     * Adds multiple items which are uniformly distributed on the TagCloud surface.
     *
     * @param items The list of items to place.
     * @param layer The layer for each item, could be used to control how "deep" item is placed
     * in the TagCloud. By default layer is set to 1, which means the sphere surface.
     * @param rotation The initial rotation for each item.
     * @param content The content displayed by a single item.
     */
    public fun <T> itemsIndexed(
        items: List<T>,
        layer: (Int, T) -> Float = { _, _ -> DefaultLayer },
        rotation: (Int, T) -> Quaternion = { _, _ -> Quaternion.Identity },
        content: @Composable TagCloudItemScope.(Int, T) -> Unit
    )
}

/**
 * Implementation of the [TagCloudScope].
 */
internal class TagCloudScopeImpl : TagCloudScope {

    private val _items: MutableList<TagCloudItem> = mutableListOf()

    /**
     * The list of registered items in TagCloud.
     */
    val items: List<TagCloudItem> = _items

    override fun item(
        coordinates: Vector3,
        content: @Composable TagCloudItemScope.() -> Unit
    ) {
        items(
            coordinates = listOf(coordinates),
            content = { content() }
        )
    }

    override fun items(
        coordinates: List<Vector3>,
        content: @Composable TagCloudItemScope.(Vector3) -> Unit
    ) {
        itemsIndexed(
            coordinates = coordinates,
            content = { _, itemCoordinates -> content(itemCoordinates) }
        )
    }

    override fun itemsIndexed(
        coordinates: List<Vector3>,
        content: @Composable TagCloudItemScope.(Int, Vector3) -> Unit
    ) {
        _items.addAll(
            coordinates.mapIndexed { index, itemCoordinates ->
                TagCloudItem(
                    coordinates = itemCoordinates,
                    content = { content(index, itemCoordinates) }
                )
            }
        )
    }

    override fun <T> items(
        items: List<T>,
        layer: (T) -> Float,
        rotation: (T) -> Quaternion,
        content: @Composable TagCloudItemScope.(T) -> Unit
    ) {
        itemsIndexed(
            items = items,
            layer = { _, item -> layer(item) },
            rotation = { _, item -> rotation(item) },
            content = { _, item -> content(item) }
        )
    }

    override fun <T> itemsIndexed(
        items: List<T>,
        layer: (Int, T) -> Float,
        rotation: (Int, T) -> Quaternion,
        content: @Composable TagCloudItemScope.(Int, T) -> Unit
    ) {
        val lattice = FibonacciLattice(items.size)
        val coordinates = items.mapIndexed { index, item ->
            var coordinates = lattice.item(index)
            val itemLayer = layer(index, item)
            if (itemLayer != DefaultLayer) {
                coordinates *= itemLayer
            }
            val itemRotation = rotation(index, item)
            if (itemRotation != Quaternion.Identity) {
                coordinates = coordinates.rotate(itemRotation)
            }
            coordinates
        }
        itemsIndexed(
            coordinates = coordinates,
            content = { index, _ -> content(index, items[index]) }
        )
    }
}

/**
 * Data of the registered item in TagCloud.
 *
 * @property coordinates The initial item position in the TagCloud (without applied rotation).
 * @property content The content of the item.
 */
internal data class TagCloudItem(
    val coordinates: Vector3,
    val content: @Composable TagCloudItemScope.() -> Unit
)

private const val DefaultLayer = 1f
