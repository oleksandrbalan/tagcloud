package eu.wewox.tagcloud

/**
 * Enumeration of available demo examples.
 *
 * @param label Example name.
 * @param description Brief description.
 */
enum class Example(
    val label: String,
    val description: String,
) {
    SimpleTagCloud(
        "Simple TagCloud",
        "Basic tag cloud usage"
    ),
    StateInTagCloud(
        "State in TagCloud",
        "Example of how to use state"
    ),
    ComponentGalleryCloud(
        "Component gallery cloud",
        "Showcases that every item in tag cloud is basically a Composable"
    ),
}
