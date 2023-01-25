package eu.wewox.tagcloud.screens

import android.graphics.Color.HSVToColor
import android.graphics.Color.RGBToHSV
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.wewox.tagcloud.Example
import eu.wewox.tagcloud.R
import eu.wewox.tagcloud.TagCloud
import eu.wewox.tagcloud.TagCloudItemScope
import eu.wewox.tagcloud.TagCloudState
import eu.wewox.tagcloud.math.Vector3
import eu.wewox.tagcloud.rememberTagCloudState
import eu.wewox.tagcloud.ui.components.TopBar
import eu.wewox.tagcloud.ui.theme.SpacingMedium
import eu.wewox.tagcloud.ui.theme.SpacingSmall
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

/**
 * Showcases how tag cloud can be customized.
 */
@Composable
fun FeaturesCloudScreen() {
    Scaffold(
        topBar = { TopBar(Example.FeaturesCloud.label) },
    ) { padding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            var visible by remember { mutableStateOf(false) }
            var autoRotation by remember { mutableStateOf(true) }

            val state = rememberTagCloudState(
                onStartGesture = { autoRotation = false },
                onEndGesture = { autoRotation = true },
            )

            LaunchedEffect(state, autoRotation) {
                while (isActive && autoRotation) {
                    delay(10)
                    state.rotateBy(0.001f, Vector3(1f, 1f, 1f))
                }
            }

            FeaturesCloud(
                state = state,
                visible = visible,
            )

            Button(
                onClick = { visible = !visible },
                modifier = Modifier
                    .padding(SpacingMedium)
                    .align(Alignment.BottomCenter)
            ) {
                Text(text = if (visible) "Hide" else "Show")
            }
        }
    }
}

@Composable
private fun FeaturesCloud(state: TagCloudState, visible: Boolean) {
    val currentVisible = rememberUpdatedState(visible)

    val (features, particles) = rememberData()

    TagCloud(
        state = state,
        modifier = Modifier.padding(64.dp)
    ) {
        itemsIndexed(features) { index, feature ->
            FeatureItem(index, feature, currentVisible.value)
        }

        itemsIndexed(
            items = particles,
            layer = { _, particle -> particle.layer }
        ) { index, particle ->
            ParticleItem(index, particle, currentVisible.value)
        }
    }
}

@Composable
private fun TagCloudItemScope.FeatureItem(
    index: Int,
    feature: Feature,
    visible: Boolean,
) {
    val spec = tween<Float>(
        durationMillis = 500,
        delayMillis = 25 * index,
        easing = CubicBezierEasing(0.5f, 1f, 0.5f, 1.5f)
    )
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(spec) + scaleIn(spec),
        exit = fadeOut(spec) + scaleOut(spec),
        modifier = Modifier
            .tagCloudItemFade()
            .tagCloudItemScaleDown()
            .padding(SpacingMedium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(feature.color, CircleShape)
                .border(1.dp, Color.Black, CircleShape)
                .padding(SpacingSmall)
        ) {
            Icon(
                painter = painterResource(feature.iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = feature.label,
                fontSize = 15.sp,
                color = Color.Black,
                modifier = Modifier.padding(start = SpacingSmall)
            )
        }
    }
}

@Composable
private fun TagCloudItemScope.ParticleItem(
    index: Int,
    particle: Particle,
    visible: Boolean,
) {
    val spec = tween<Float>(
        durationMillis = 1000,
        delayMillis = 5 * index
    )
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(spec) + scaleIn(spec),
        exit = fadeOut(spec) + scaleOut(spec),
        modifier = Modifier
            .tagCloudItemFade()
            .tagCloudItemScaleDown()
    ) {
        val color = particle.color.shifted(360f * coordinates.x * coordinates.y).toColor()
        Box(
            modifier = Modifier
                .size(particle.size)
                .background(color, CircleShape)
        )
    }
}

@Composable
private fun rememberData(): Pair<List<Feature>, List<Particle>> {
    val features = remember {
        listOf(
            Feature("Channels", R.drawable.ic_channels, BrandColor1),
            Feature("Connect", R.drawable.ic_connect, BrandColor2),
            Feature("Messaging", R.drawable.ic_messaging, BrandColor3),
            Feature("Huddle", R.drawable.ic_huddle, BrandColor4),
            Feature("Accessibility", R.drawable.ic_accesibility, BrandColor1),
            Feature("Integrations", R.drawable.ic_integrations, BrandColor2),
            Feature("Workflow", R.drawable.ic_workflow, BrandColor3),
            Feature("Search", R.drawable.ic_search, BrandColor4),
            Feature("Files", R.drawable.ic_files, BrandColor1),
            Feature("Security", R.drawable.ic_security, BrandColor2),
            Feature("Enterprise", R.drawable.ic_enterprise, BrandColor3),
            Feature("Atlas", R.drawable.ic_atlas, BrandColor4),
        )
    }

    val background = MaterialTheme.colorScheme.background
    val particles = remember {
        List(32) {
            val color = BrandColors.random()
                .copy(alpha = ParticleAlpha)
                .compositeOver(background)
                .toHsvColor()
            val size = Random.nextInt(ParticleSizeMin, ParticleSizeMax).dp
            val layer = Random.nextFloat().rescale(ParticleLayerMin, ParticleLayerMax)
            Particle(color, size, layer)
        }
    }

    return features to particles
}

private data class Feature(
    val label: String,
    val iconRes: Int,
    val color: Color,
)

private data class Particle(
    val color: HsvColor,
    val size: Dp,
    val layer: Float,
)

private data class HsvColor(
    val h: Float,
    val s: Float,
    val v: Float,
) {
    fun shifted(hue: Float): HsvColor =
        HsvColor(h + hue, s, v)

    fun toColor(): Color =
        Color(HSVToColor(floatArrayOf(h, s, v)))
}

private fun Color.toHsvColor(): HsvColor {
    val hsv = FloatArray(3)
    RGBToHSV(
        (red * 256).toInt(),
        (green * 256).toInt(),
        (blue * 256).toInt(),
        hsv
    )
    return HsvColor(hsv[0], hsv[1], hsv[2])
}

private fun Float.rescale(newMin: Float, newMax: Float): Float =
    (((this + 1f) * (newMax - newMin)) / 2f) + newMin

private const val ParticleAlpha = 0.3f
private const val ParticleSizeMin = 8
private const val ParticleSizeMax = 16
private const val ParticleLayerMin = 0.1f
private const val ParticleLayerMax = 0.9f

private val BrandColor1 = Color(0xFF36C5F0)
private val BrandColor2 = Color(0xFF2EB67D)
private val BrandColor3 = Color(0xFFE01E5A)
private val BrandColor4 = Color(0xFFECB22E)

private val BrandColors: List<Color> = listOf(BrandColor1, BrandColor2, BrandColor3, BrandColor4)
