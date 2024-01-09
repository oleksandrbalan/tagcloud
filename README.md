[![Maven Central](https://img.shields.io/maven-central/v/io.github.oleksandrbalan/tagcloud.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.oleksandrbalan/tagcloud)

<img align="right" src="https://user-images.githubusercontent.com/20944869/214668307-c4f4381e-c533-40f9-b5db-b21f5f2db54a.png">

# Tag Cloud

Tag cloud as 3D sphere.

Allows to place items on the sphere to create a tag cloud.

## Multiplatform

Library supports [Android](https://developer.android.com/jetpack/compose), [iOS](https://github.com/JetBrains/compose-multiplatform-ios-android-template/#readme) and [Desktop](https://github.com/JetBrains/compose-multiplatform-desktop-template/#readme) (Windows, MacOS, Linux) targets.

## Examples

See Demo application and [examples](demo/src/commonMain/kotlin/eu/wewox/tagcloud/screens).

### Simple tag cloud

<img src="https://user-images.githubusercontent.com/20944869/214673568-c972c32c-c57e-4309-9e62-e7352a0b093e.gif" width="450">

### Tags placed on axis

<img src="https://user-images.githubusercontent.com/20944869/214674832-fdf66ec6-aa77-4d36-86a1-746c65ca91f7.gif" width="450">

### Fancy tag cloud with particles inside

<img src="https://user-images.githubusercontent.com/20944869/214677164-78f2eef7-1778-43ef-9804-223332900a80.gif" width="450">


## Usage

### Get a dependency

**Step 1.** Add the MavenCentral repository to your build file.
Add it in your root `build.gradle.kts` at the end of repositories:
```kotlin
allprojects {
    repositories {
        ...
        mavenCentral()
    }
}
```

Or in `settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    repositories {
        ...
        mavenCentral()
    }
}
```

**Step 2.** Add the dependency.
Check latest version on the [releases page](https://github.com/oleksandrbalan/tagcloud/releases).
```kotlin
dependencies {
    implementation("io.github.oleksandrbalan:tagcloud:$version")
}
```

### Use in Composable

The `TagCloud` has 2 mandatory arguments:
* **state** - The state of the TagCloud, used to observe and change it's rotation.
* **content** - The content lambda to register items to be shown in the TagCloud.

Inside `content` lambda use `item` or `items` method to register items in the TagCloud. Each method has own `content` lambda to specify how item is displayed. Inside item scope you could access item's coordinates to adjust appearance based on where item is currently in the tag cloud. Or you may use `.tagCloudItemFade()` and / or `.tagCloudItemScaleDown()` modifiers to use out-of-box behavior.

```
val labels = List(32) { "Item #$it" }

TagCloud(
    state = rememberTagCloudState(),
    modifier = Modifier.padding(64.dp)
) {
    items(labels) {
        Text(
            text = it,
            modifier = Modifier
                .tagCloudItemFade()
                .tagCloudItemScaleDown()
        )
    }
}
```

See Demo application and [examples](demo/src/commonMain/kotlin/eu/wewox/tagcloud/screens) for more usage examples.

## TODO list

* Add ability to focus an item
* Add content padding for the TagCloud to expand gesture area
* Add fling support
* Add `animateTo` / `animateBy` methods to the `TagCloudState`
* Add ability to change where item is facing
* Fix item clipping when scale is greater than 1
