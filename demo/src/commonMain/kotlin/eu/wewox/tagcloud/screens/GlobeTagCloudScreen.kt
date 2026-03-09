package eu.wewox.tagcloud.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import eu.wewox.tagcloud.Example
import eu.wewox.tagcloud.TagCloud
import eu.wewox.tagcloud.TagCloudState
import eu.wewox.tagcloud.math.Quaternion
import eu.wewox.tagcloud.math.Vector3
import eu.wewox.tagcloud.rememberTagCloudState
import eu.wewox.tagcloud.ui.components.TopBar
import eu.wewox.tagcloud.ui.theme.SpacingSmall
import kotlinx.coroutines.launch

/**
 * Spinning globe with dots.
 */
@Composable
fun GlobeCloudScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopBar(
                title = Example.GlobeCloud.label,
                onBackClick = onBackClick
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding),
            verticalArrangement = Arrangement.Center,
        ) {
            val scope = rememberCoroutineScope()
            val state = rememberTagCloudState()

            var selected: City? by remember { mutableStateOf(null) }

            GlobeCloud(
                selected = selected,
                state = state,
            )

            CitiesColumn(
                selected = selected,
                onCityClick = {
                    selected = it
                    scope.launch {
                        state.animateRotateTo(
                            Quaternion.create(
                                from = it.coordinates,
                                to = Vector3(0f, 0f, 1f)
                            ),
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun GlobeCloud(
    selected: City?,
    state: TagCloudState,
    modifier: Modifier = Modifier,
) {
    val selectedUpdated by rememberUpdatedState(selected)
    TagCloud(
        modifier = modifier,
        state = state,
        contentPadding = PaddingValues(64.dp),
    ) {
        items(
            items = WorldPointsList,
            coordinates = { it.coordinates },
        ) {
            Spacer(
                modifier = Modifier
                    .tagCloudItemStyle()
                    .size(6.dp)
                    .background(
                        color = getTopographicColor(it.elevation),
                        shape = CircleShape,
                    ),
            )
        }

        items(
            items = CitiesList,
            coordinates = { it.coordinates },
        ) {
            val alpha by animateFloatAsState(if (it == selectedUpdated) 1f else 0f)
            Box(
                modifier = Modifier
                    .height(64.dp)
                    .tagCloudItemStyle()
                    .alpha(alpha),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter),
                    shape = RoundedCornerShape(SpacingSmall),
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Text(
                        text = it.name,
                        modifier = Modifier
                            .padding(
                                horizontal = 4.dp,
                                vertical = 2.dp,
                            )
                    )
                }
                Spacer(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                        ),
                )
            }
        }
    }
}

@Composable
private fun CitiesColumn(
    selected: City?,
    onCityClick: (City) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        CitiesList.forEach {
            ListItem(
                modifier = Modifier
                    .clickable {
                        onCityClick(it)
                    },
                headlineContent = {
                    Text(it.name)
                },
                trailingContent = {
                    if (it == selected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                        )
                    }
                }
            )
        }
    }
}

private fun getTopographicColor(elevation: Float): Color {
    val landStops = listOf(
        0f to Color(0xFF336600),
        500f to Color(0xFF9ACD32),
        1000f to Color(0xFFE8D0A5),
        2000f to Color(0xFFC2A378),
        3000f to Color(0xFF8B4513),
        4000f to Color(0xFF808080),
        5500f to Color(0xFFCFD8DC)
    )

    for (i in 0 until landStops.size - 1) {
        val (minElev, startColor) = landStops[i]
        val (maxElev, endColor) = landStops[i + 1]

        if (elevation <= maxElev) {
            val fraction = ((elevation - minElev) / (maxElev - minElev)).coerceIn(0f, 1f)
            return lerp(startColor, endColor, fraction)
        }
    }

    return landStops.last().second
}

private data class WorldPoint(
    val elevation: Float,
    val coordinates: Vector3,
)

/**
 * Elevation source: https://open-meteo.com/en/docs/elevation-api.
 */
private val WorldPointsList = listOf(
    WorldPoint(34.0f, Vector3(-0.018430382f, 0.09295198f, 0.9955f)),
    WorldPoint(187.0f, Vector3(-0.09496389f, 0.09945657f, 0.9905f)),
    WorldPoint(350.0f, Vector3(0.024241379f, 0.15574372f, 0.9875f)),
    WorldPoint(54.0f, Vector3(0.1539412f, 0.08403486f, 0.9845f)),
    WorldPoint(303.0f, Vector3(-0.07461213f, 0.1706657f, 0.9825f)),
    WorldPoint(264.0f, Vector3(0.09738262f, 0.17634165f, 0.9795f)),
    WorldPoint(121.0f, Vector3(-0.17502826f, 0.11772364f, 0.9775f)),
    WorldPoint(606.0f, Vector3(0.21311243f, 0.032106757f, 0.9765f)),
    WorldPoint(314.0f, Vector3(-0.013901857f, 0.22395644f, 0.9745f)),
    WorldPoint(169.0f, Vector3(0.17881803f, 0.1556016f, 0.9715f)),
    WorldPoint(378.0f, Vector3(-0.14410672f, 0.19824985f, 0.9695f)),
    WorldPoint(613.0f, Vector3(0.24401924f, -0.04962218f, 0.9685f)),
    WorldPoint(283.0f, Vector3(0.07294062f, 0.24608417f, 0.9665f)),
    WorldPoint(899.0f, Vector3(0.24998312f, 0.09579243f, 0.9635f)),
    WorldPoint(263.0f, Vector3(-0.075520955f, 0.26422402f, 0.9615f)),
    WorldPoint(225.0f, Vector3(0.23712873f, -0.14563555f, 0.9605f)),
    WorldPoint(354.0f, Vector3(0.16986822f, 0.2289597f, 0.9585f)),
    WorldPoint(948.0f, Vector3(-0.21819833f, 0.19364204f, 0.9565f)),
    WorldPoint(330.0f, Vector3(0.29495433f, 0.004656683f, 0.9555f)),
    WorldPoint(397.0f, Vector3(0.021606693f, 0.30061755f, 0.9535f)),
    WorldPoint(329.0f, Vector3(0.25951013f, 0.17089246f, 0.9505f)),
    WorldPoint(190.0f, Vector3(-0.15042438f, 0.27878353f, 0.9485f)),
    WorldPoint(781.0f, Vector3(0.3020475f, -0.10493358f, 0.9475f)),
    WorldPoint(690.0f, Vector3(0.13334075f, 0.297069f, 0.9455f)),
    WorldPoint(419.0f, Vector3(0.32523757f, 0.07690428f, 0.9425f)),
    WorldPoint(286.0f, Vector3(-0.048981134f, 0.33624485f, 0.9405f)),
    WorldPoint(1451.0f, Vector3(0.26538184f, -0.21659231f, 0.9395f)),
    WorldPoint(332.0f, Vector3(0.24275663f, 0.24932502f, 0.9375f)),
    WorldPoint(52.0f, Vector3(-0.2306338f, 0.26767108f, 0.9355f)),
    WorldPoint(430.0f, Vector3(0.35348952f, -0.0418917f, 0.9345f)),
    WorldPoint(537.0f, Vector3(0.07369785f, 0.3535709f, 0.9325f)),
    WorldPoint(384.0f, Vector3(0.33232978f, 0.15995833f, 0.9295f)),
    WorldPoint(375.0f, Vector3(-0.13205838f, 0.34972036f, 0.9275f)),
    WorldPoint(1148.0f, Vector3(0.33581418f, -0.16978395f, 0.9265f)),
    WorldPoint(428.0f, Vector3(0.20107065f, 0.32383692f, 0.9245f)),
    WorldPoint(371.0f, Vector3(0.38649788f, 0.038172442f, 0.9215f)),
    WorldPoint(336.0f, Vector3(-0.0042878203f, 0.3930666f, 0.9195f)),
    WorldPoint(1140.0f, Vector3(0.2702676f, -0.28864023f, 0.9185f)),
    WorldPoint(349.0f, Vector3(0.31479397f, 0.2468451f, 0.9165f)),
    WorldPoint(146.0f, Vector3(-0.22086336f, 0.3389825f, 0.9145f)),
    WorldPoint(537.0f, Vector3(0.39401898f, -0.10132516f, 0.9135f)),
    WorldPoint(848.0f, Vector3(0.13686919f, 0.38785896f, 0.9115f)),
    WorldPoint(677.0f, Vector3(0.3973428f, 0.1294081f, 0.9085f)),
    WorldPoint(312.0f, Vector3(-0.09514839f, 0.4113448f, 0.9065f)),
    WorldPoint(1192.0f, Vector3(0.3497832f, -0.24025288f, 0.9055f)),
    WorldPoint(561.0f, Vector3(0.2726111f, 0.33071277f, 0.9035f)),
    WorldPoint(499.0f, Vector3(0.4345851f, -0.015347092f, 0.9005f)),
    WorldPoint(369.0f, Vector3(0.05378676f, 0.43566585f, 0.8985f)),
    WorldPoint(1436.0f, Vector3(0.2558112f, -0.35924137f, 0.8975f)),
    WorldPoint(841.0f, Vector3(0.3837196f, 0.22547509f, 0.8955f)),
    WorldPoint(251.0f, Vector3(-0.19278271f, 0.40557685f, 0.8935f)),
    WorldPoint(887.0f, Vector3(0.41795382f, -0.1695829f, 0.8925f)),
    WorldPoint(678.0f, Vector3(0.20726645f, 0.40503132f, 0.8905f)),
    WorldPoint(667.0f, Vector3(0.45327133f, 0.082999066f, 0.8875f)),
    WorldPoint(442.0f, Vector3(-0.043411683f, 0.46260694f, 0.8855f)),
    WorldPoint(1064.0f, Vector3(0.34590623f, -0.3130633f, 0.8845f)),
    WorldPoint(497.0f, Vector3(0.3448718f, 0.3197768f, 0.8825f)),
    WorldPoint(650.0f, Vector3(0.46911538f, -0.08006575f, 0.8795f)),
    WorldPoint(513.0f, Vector3(0.12170526f, 0.4638767f, 0.8775f)),
    WorldPoint(429.0f, Vector3(0.44720712f, 0.18776461f, 0.8745f)),
    WorldPoint(531.0f, Vector3(-0.14904289f, 0.4653278f, 0.8725f)),
    WorldPoint(1136.0f, Vector3(0.42577064f, -0.2433251f, 0.8715f)),
    WorldPoint(473.0f, Vector3(0.28163373f, 0.40577358f, 0.8695f)),
    WorldPoint(970.0f, Vector3(0.49861738f, 0.02363194f, 0.8665f)),
    WorldPoint(422.0f, Vector3(0.02018934f, 0.5022272f, 0.8645f)),
    WorldPoint(1238.0f, Vector3(0.32547f, -0.38527527f, 0.8635f)),
    WorldPoint(588.0f, Vector3(0.41504082f, 0.2925045f, 0.8615f)),
    WorldPoint(927.0f, Vector3(0.48948473f, -0.15291323f, 0.8585f)),
    WorldPoint(554.0f, Vector3(0.19638965f, 0.4773247f, 0.8565f)),
    WorldPoint(402.0f, Vector3(0.50307107f, 0.13585737f, 0.8535f)),
    WorldPoint(1627.0f, Vector3(-0.09192767f, 0.5162335f, 0.8515f)),
    WorldPoint(966.0f, Vector3(0.41778246f, -0.31954274f, 0.8505f)),
    WorldPoint(484.0f, Vector3(0.35701674f, 0.3906236f, 0.8485f)),
    WorldPoint(1324.0f, Vector3(0.53198427f, -0.04607058f, 0.8455f)),
    WorldPoint(147.0f, Vector3(0.09294904f, 0.52902573f, 0.8435f)),
    WorldPoint(1210.0f, Vector3(0.2896567f, -0.45419464f, 0.8425f)),
    WorldPoint(509.0f, Vector3(0.4805419f, 0.25027832f, 0.8405f)),
    WorldPoint(1263.0f, Vector3(0.4951934f, -0.23103516f, 0.8375f)),
    WorldPoint(187.0f, Vector3(0.27497098f, 0.47574228f, 0.8355f)),
    WorldPoint(911.0f, Vector3(0.5493488f, 0.071830824f, 0.8325f)),
    WorldPoint(1070.0f, Vector3(-0.023655703f, 0.5565161f, 0.8305f)),
    WorldPoint(1065.0f, Vector3(0.39441508f, -0.39543203f, 0.8295f)),
    WorldPoint(409.0f, Vector3(0.43066967f, 0.3602324f, 0.8275f)),
    WorldPoint(1295.0f, Vector3(0.55220705f, -0.12356031f, 0.8245f)),
    WorldPoint(22.0f, Vector3(0.17224349f, 0.54205716f, 0.8225f)),
    WorldPoint(469.0f, Vector3(0.53903437f, 0.19458084f, 0.8195f)),
    WorldPoint(1321.0f, Vector3(0.48598006f, -0.31169075f, 0.8165f)),
    WorldPoint(170.0f, Vector3(0.35470742f, 0.45909953f, 0.8145f)),
    WorldPoint(2545.0f, Vector3(0.5843484f, -0.0021776934f, 0.8115f)),
    WorldPoint(589.0f, Vector3(0.053508654f, 0.58467644f, 0.8095f)),
    WorldPoint(1498.0f, Vector3(0.35632098f, -0.46836215f, 0.8085f)),
    WorldPoint(256.0f, Vector3(0.500036f, 0.31547064f, 0.8065f)),
    WorldPoint(475.0f, Vector3(0.5584175f, -0.206295f, 0.8035f)),
    WorldPoint(2463.0f, Vector3(0.58843136f, 0.12706797f, 0.7985f)),
    WorldPoint(267.0f, Vector3(-0.07546736f, 0.5999104f, 0.7965f)),
    WorldPoint(731.0f, Vector3(0.4618833f, -0.39222896f, 0.7955f)),
    WorldPoint(271.0f, Vector3(0.43296927f, 0.42766267f, 0.7935f)),
    WorldPoint(930.0f, Vector3(0.60668385f, -0.08393147f, 0.7905f)),
    WorldPoint(151.0f, Vector3(0.13722388f, 0.59953094f, 0.7885f)),
    WorldPoint(1471.0f, Vector3(0.3044249f, -0.5358817f, 0.7875f)),
    WorldPoint(477.0f, Vector3(0.56276053f, 0.2574691f, 0.7855f)),
    WorldPoint(478.0f, Vector3(0.55008036f, -0.2917282f, 0.7825f)),
    WorldPoint(483.0f, Vector3(-0.6038861f, -0.15677792f, 0.7815f)),
    WorldPoint(170.0f, Vector3(0.34006512f, 0.5245717f, 0.7805f)),
    WorldPoint(512.0f, Vector3(0.6269249f, 0.04958777f, 0.7775f)),
    WorldPoint(1687.0f, Vector3(0.42326805f, -0.4700999f, 0.7745f)),
    WorldPoint(237.0f, Vector3(0.5072542f, 0.38201696f, 0.7725f)),
    WorldPoint(261.0f, Vector3(0.6153014f, -0.17109638f, 0.7695f)),
    WorldPoint(2897.0f, Vector3(0.6167142f, 0.18762568f, 0.7645f)),
    WorldPoint(153.0f, Vector3(0.527012f, -0.37732756f, 0.7615f)),
    WorldPoint(228.0f, Vector3(-0.6446479f, -0.07790263f, 0.7605f)),
    WorldPoint(176.0f, Vector3(0.4234821f, 0.493784f, 0.7595f)),
    WorldPoint(17.0f, Vector3(0.6530115f, -0.035829324f, 0.7565f)),
    WorldPoint(589.0f, Vector3(0.37083173f, -0.5428827f, 0.7535f)),
    WorldPoint(358.0f, Vector3(0.6094985f, -0.26126486f, 0.7485f)),
    WorldPoint(220.0f, Vector3(-0.6269708f, -0.21943404f, 0.7475f)),
    WorldPoint(658.0f, Vector3(0.6600239f, 0.10759249f, 0.7435f)),
    WorldPoint(876.0f, Vector3(-0.049641475f, 0.66911393f, 0.7415f)),
    WorldPoint(32.0f, Vector3(0.18528424f, 0.65394765f, 0.7335f)),
    WorldPoint(47.0f, Vector3(0.63470316f, 0.252035f, 0.7305f)),
    WorldPoint(295.0f, Vector3(-0.6730672f, -0.1384857f, 0.7265f)),
    WorldPoint(43.0f, Vector3(0.69110286f, 0.019249642f, 0.7225f)),
    WorldPoint(744.0f, Vector3(0.04136856f, 0.6922199f, 0.7205f)),
    WorldPoint(638.0f, Vector3(-0.639953f, -0.28526813f, 0.7135f)),
    WorldPoint(600.0f, Vector3(0.2795908f, 0.64356256f, 0.7125f)),
    WorldPoint(1048.0f, Vector3(0.6837889f, 0.17041856f, 0.7095f)),
    WorldPoint(36.0f, Vector3(-0.7070149f, -0.048986558f, 0.7055f)),
    WorldPoint(417.0f, Vector3(0.48892567f, 0.51442343f, 0.7045f)),
    WorldPoint(59.0f, Vector3(0.13737558f, 0.70130426f, 0.6995f)),
    WorldPoint(1842.0f, Vector3(0.6427596f, 0.31897932f, 0.6965f)),
    WorldPoint(744.0f, Vector3(-0.69219923f, -0.20323384f, 0.6925f)),
    WorldPoint(793.0f, Vector3(0.37361947f, 0.6182526f, 0.6915f)),
    WorldPoint(183.0f, Vector3(0.72081274f, 0.07997959f, 0.6885f)),
    WorldPoint(57.0f, Vector3(-0.01669926f, 0.726938f, 0.6865f)),
    WorldPoint(976.0f, Vector3(0.5689366f, 0.45731708f, 0.6835f)),
    WorldPoint(816.0f, Vector3(-0.64316136f, -0.35302013f, 0.6795f)),
    WorldPoint(592.0f, Vector3(0.23608272f, 0.69563115f, 0.6785f)),
    WorldPoint(863.0f, Vector3(0.69830877f, 0.23677975f, 0.6755f)),
    WorldPoint(181.0f, Vector3(-0.73247945f, -0.11207869f, 0.6715f)),
    WorldPoint(777.0f, Vector3(0.08251497f, 0.7418228f, 0.6655f)),
    WorldPoint(1136.0f, Vector3(0.64119685f, 0.38724712f, 0.6625f)),
    WorldPoint(40.0f, Vector3(0.69990927f, -0.27420208f, 0.6595f)),
    WorldPoint(930.0f, Vector3(-0.7021296f, -0.27090922f, 0.6585f)),
    WorldPoint(172.0f, Vector3(0.33512062f, 0.67482436f, 0.6575f)),
    WorldPoint(488.0f, Vector3(0.74200046f, 0.14513795f, 0.6545f)),
    WorldPoint(6.0f, Vector3(-0.759382f, -0.013738505f, 0.6505f)),
    WorldPoint(734.0f, Vector3(0.55128455f, 0.5236746f, 0.6495f)),
    WorldPoint(667.0f, Vector3(0.18510556f, 0.7418596f, 0.6445f)),
    WorldPoint(666.0f, Vector3(0.70367616f, 0.3054793f, 0.6415f)),
    WorldPoint(787.0f, Vector3(0.6727568f, -0.37378606f, 0.6385f)),
    WorldPoint(182.0f, Vector3(-0.7493647f, -0.17901501f, 0.6375f)),
    WorldPoint(883.0f, Vector3(0.4320982f, 0.63887316f, 0.6365f)),
    WorldPoint(92.0f, Vector3(0.021799278f, 0.77506936f, 0.6315f)),
    WorldPoint(551.0f, Vector3(0.63033146f, 0.4557083f, 0.6285f)),
    WorldPoint(514.0f, Vector3(-0.7029659f, -0.34035084f, 0.6245f)),
    WorldPoint(532.0f, Vector3(0.2887364f, 0.72655284f, 0.6235f)),
    WorldPoint(178.0f, Vector3(-0.7833623f, -0.079191014f, 0.6165f)),
    WorldPoint(308.0f, Vector3(0.5246548f, 0.5881302f, 0.6155f)),
    WorldPoint(148.0f, Vector3(0.12763949f, 0.78166354f, 0.6105f)),
    WorldPoint(236.0f, Vector3(0.7000196f, 0.37538826f, 0.6075f)),
    WorldPoint(249.0f, Vector3(-0.75759614f, -0.24866821f, 0.6035f)),
    WorldPoint(25.0f, Vector3(-0.043713063f, 0.8006765f, 0.5975f)),
    WorldPoint(276.0f, Vector3(-0.802918f, 0.026503077f, 0.5955f)),
    WorldPoint(8.0f, Vector3(0.6105114f, 0.52330256f, 0.5945f)),
    WorldPoint(916.0f, Vector3(-0.69486177f, -0.41045934f, 0.5905f)),
    WorldPoint(147.0f, Vector3(0.23532102f, 0.77273136f, 0.5895f)),
    WorldPoint(113.0f, Vector3(0.7584609f, 0.28417403f, 0.5865f)),
    WorldPoint(319.0f, Vector3(-0.7991978f, -0.14824532f, 0.5825f)),
    WorldPoint(2431.0f, Vector3(0.489596f, 0.6497334f, 0.5815f)),
    WorldPoint(722.0f, Vector3(-0.7571641f, -0.31995663f, 0.5695f)),
    WorldPoint(182.0f, Vector3(0.3424697f, 0.7480122f, 0.5685f)),
    WorldPoint(108.0f, Vector3(-0.8264565f, -0.041077852f, 0.5615f)),
    WorldPoint(1963.0f, Vector3(0.582132f, 0.5890349f, 0.5605f)),
    WorldPoint(977.0f, Vector3(-0.6780303f, -0.48019025f, 0.5565f)),
    WorldPoint(139.0f, Vector3(0.17573595f, 0.81273407f, 0.5555f)),
    WorldPoint(145.0f, Vector3(0.7537162f, 0.35588717f, 0.5525f)),
    WorldPoint(349.0f, Vector3(-0.8067284f, -0.2198567f, 0.5485f)),
    WorldPoint(37.0f, Vector3(0.4466951f, 0.7076067f, 0.5475f)),
    WorldPoint(216.0f, Vector3(-0.83837163f, 0.07065911f, 0.5405f)),
    WorldPoint(2129.0f, Vector3(0.66642654f, 0.51460224f, 0.5395f)),
    WorldPoint(309.0f, Vector3(-0.74813217f, -0.3918392f, 0.5355f)),
    WorldPoint(134.0f, Vector3(0.287181f, 0.79488164f, 0.5345f)),
    WorldPoint(120.0f, Vector3(-0.8421284f, -0.112086974f, 0.5275f)),
    WorldPoint(922.0f, Vector3(0.54564357f, 0.65197456f, 0.5265f)),
    WorldPoint(4.0f, Vector3(0.1108563f, 0.84601927f, 0.5215f)),
    WorldPoint(96.0f, Vector3(-0.80587685f, -0.2930055f, 0.5145f)),
    WorldPoint(141.0f, Vector3(0.3965831f, 0.7609465f, 0.5135f)),
    WorldPoint(140.0f, Vector3(-0.86223906f, 0.0012273703f, 0.5065f)),
    WorldPoint(1266.0f, Vector3(0.6370505f, 0.58192474f, 0.5055f)),
    WorldPoint(185.0f, Vector3(-0.7306416f, -0.4633148f, 0.5015f)),
    WorldPoint(17.0f, Vector3(0.2259388f, 0.83573407f, 0.5005f)),
    WorldPoint(282.0f, Vector3(-0.84972465f, -0.18554196f, 0.4935f)),
    WorldPoint(6.0f, Vector3(0.50155526f, 0.7112567f, 0.4925f)),
    WorldPoint(121.0f, Vector3(-0.8662624f, 0.11781004f, 0.4855f)),
    WorldPoint(2643.0f, Vector3(0.7187902f, 0.4985985f, 0.4845f)),
    WorldPoint(104.0f, Vector3(-0.7966508f, -0.3666977f, 0.4805f)),
    WorldPoint(244.0f, Vector3(0.33993673f, 0.8090258f, 0.4795f)),
    WorldPoint(25.0f, Vector3(-0.8784242f, -0.071516804f, 0.4725f)),
    WorldPoint(183.0f, Vector3(-0.7049131f, -0.53342414f, 0.4675f)),
    WorldPoint(264.0f, Vector3(-0.8491256f, -0.2604715f, 0.4595f)),
    WorldPoint(106.0f, Vector3(0.4504347f, 0.7660851f, 0.4585f)),
    WorldPoint(369.0f, Vector3(-0.8910413f, 0.046831317f, 0.4515f)),
    WorldPoint(718.0f, Vector3(0.6890502f, 0.5676791f, 0.4505f)),
    WorldPoint(95.0f, Vector3(-0.7791443f, -0.4399681f, 0.4465f)),
    WorldPoint(78.0f, Vector3(0.27747703f, 0.85119694f, 0.4455f)),
    WorldPoint(74.0f, Vector3(-0.8866875f, -0.1466389f, 0.4385f)),
    WorldPoint(5.0f, Vector3(-0.67124647f, -0.6012536f, 0.4335f)),
    WorldPoint(77.0f, Vector3(0.08886259f, 0.8972442f, 0.4325f)),
    WorldPoint(363.0f, Vector3(-0.88697994f, 0.16714172f, 0.4305f)),
    WorldPoint(1555.0f, Vector3(0.7673387f, 0.47615236f, 0.4295f)),
    WorldPoint(731.0f, Vector3(-0.84030235f, -0.33591923f, 0.4255f)),
    WorldPoint(113.0f, Vector3(0.39290634f, 0.81573546f, 0.4245f)),
    WorldPoint(60.0f, Vector3(-0.908264f, -0.027392032f, 0.4175f)),
    WorldPoint(67.0f, Vector3(0.6515513f, 0.6340415f, 0.4165f)),
    WorldPoint(77.0f, Vector3(-0.753537f, -0.51188445f, 0.4125f)),
    WorldPoint(128.0f, Vector3(0.20996591f, 0.88689464f, 0.4115f)),
    WorldPoint(141.0f, Vector3(-0.88688165f, -0.22320554f, 0.4045f)),
    WorldPoint(9.0f, Vector3(0.50342876f, 0.76403356f, 0.4035f)),
    WorldPoint(419.0f, Vector3(-0.9131129f, 0.09493455f, 0.3965f)),
    WorldPoint(1204.0f, Vector3(0.73777485f, 0.547054f, 0.3955f)),
    WorldPoint(1053.0f, Vector3(-0.8233157f, -0.41094902f, 0.3915f)),
    WorldPoint(119.0f, Vector3(0.3296474f, 0.8595594f, 0.3905f)),
    WorldPoint(131.0f, Vector3(-0.9176707f, -0.10396263f, 0.3835f)),
    WorldPoint(147.0f, Vector3(0.60670835f, 0.69684917f, 0.3825f)),
    WorldPoint(89.0f, Vector3(-0.72009283f, -0.58155316f, 0.3785f)),
    WorldPoint(319.0f, Vector3(0.13820143f, 0.9156386f, 0.3775f)),
    WorldPoint(680.0f, Vector3(0.8118044f, 0.44802156f, 0.3745f)),
    WorldPoint(4393.0f, Vector3(-0.8789514f, -0.3002903f, 0.3705f)),
    WorldPoint(129.0f, Vector3(0.44545913f, 0.81549734f, 0.3695f)),
    WorldPoint(142.0f, Vector3(-0.9317795f, 0.019511197f, 0.3625f)),
    WorldPoint(198.0f, Vector3(0.7005366f, 0.61527735f, 0.3615f)),
    WorldPoint(275.0f, Vector3(-0.5521339f, 0.7522685f, 0.3595f)),
    WorldPoint(319.0f, Vector3(-0.79831535f, -0.48465076f, 0.3575f)),
    WorldPoint(75.0f, Vector3(0.2613829f, 0.89698756f, 0.3565f)),
    WorldPoint(288.0f, Vector3(-0.9190942f, -0.18197697f, 0.3495f)),
    WorldPoint(282.0f, Vector3(0.5550083f, 0.75532347f, 0.3485f)),
    WorldPoint(46.0f, Vector3(-0.6791568f, -0.6481249f, 0.3445f)),
    WorldPoint(110.0f, Vector3(-0.9286601f, 0.14480375f, 0.3415f)),
    WorldPoint(1494.0f, Vector3(0.78288066f, 0.520728f, 0.3405f)),
    WorldPoint(2918.0f, Vector3(-0.86293316f, -0.37698027f, 0.3365f)),
    WorldPoint(212.0f, Vector3(0.38179812f, 0.8612026f, 0.3355f)),
    WorldPoint(136.0f, Vector3(-0.9427054f, -0.058260165f, 0.3285f)),
    WorldPoint(236.0f, Vector3(0.65600854f, 0.67999744f, 0.3275f)),
    WorldPoint(69.0f, Vector3(-0.02439751f, -0.9448823f, 0.3265f)),
    WorldPoint(212.0f, Vector3(-0.62049514f, 0.71346724f, 0.3255f)),
    WorldPoint(442.0f, Vector3(-0.7655378f, -0.55614716f, 0.3235f)),
    WorldPoint(84.0f, Vector3(0.18887982f, 0.9275334f, 0.3225f)),
    WorldPoint(42.0f, Vector3(0.85192716f, 0.41489738f, 0.3195f)),
    WorldPoint(2103.0f, Vector3(-0.3390687f, 0.88556546f, 0.3175f)),
    WorldPoint(4348.0f, Vector3(-0.9124597f, -0.26053244f, 0.3155f)),
    WorldPoint(574.0f, Vector3(0.49700597f, 0.8087489f, 0.3145f)),
    WorldPoint(219.0f, Vector3(-0.9490812f, 0.06847396f, 0.3075f)),
    WorldPoint(1515.0f, Vector3(0.7463293f, 0.59080476f, 0.3065f)),
    WorldPoint(452.0f, Vector3(-0.523647f, 0.79565924f, 0.3045f)),
    WorldPoint(1200.0f, Vector3(-0.8389536f, -0.45238322f, 0.3025f)),
    WorldPoint(78.0f, Vector3(0.31314185f, 0.90057755f, 0.3015f)),
    WorldPoint(2890.0f, Vector3(-0.21951024f, 0.9294638f, 0.2965f)),
    WorldPoint(200.0f, Vector3(-0.945708f, -0.1374997f, 0.2945f)),
    WorldPoint(103.0f, Vector3(0.60465044f, 0.7404428f, 0.2935f)),
    WorldPoint(2929.0f, Vector3(0.05432891f, -0.954721f, 0.2925f)),
    WorldPoint(314.0f, Vector3(-0.7253029f, -0.62460023f, 0.2895f)),
    WorldPoint(48.0f, Vector3(-0.9378689f, 0.1957542f, 0.2865f)),
    WorldPoint(100.0f, Vector3(0.824042f, 0.48933065f, 0.2855f)),
    WorldPoint(209.0f, Vector3(0.43331835f, 0.8564782f, 0.2805f)),
    WorldPoint(181.0f, Vector3(-0.9618176f, -0.010230609f, 0.2735f)),
    WorldPoint(310.0f, Vector3(0.7025109f, 0.6574361f, 0.2725f)),
    WorldPoint(690.0f, Vector3(-0.07393968f, -0.959594f, 0.2715f)),
    WorldPoint(42.0f, Vector3(0.8874614f, 0.37742805f, 0.2645f)),
    WorldPoint(1354.0f, Vector3(-0.29856497f, 0.9175798f, 0.2625f)),
    WorldPoint(3554.0f, Vector3(-0.94069666f, -0.21732368f, 0.2605f)),
    WorldPoint(161.0f, Vector3(0.5469929f, 0.79590106f, 0.2595f)),
    WorldPoint(2663.0f, Vector3(0.13432519f, -0.9566266f, 0.2585f)),
    WorldPoint(1006.0f, Vector3(-0.6780104f, -0.6892181f, 0.2555f)),
    WorldPoint(1856.0f, Vector3(-0.9602738f, 0.118819244f, 0.2525f)),
    WorldPoint(338.0f, Vector3(0.7885494f, 0.561193f, 0.2515f)),
    WorldPoint(475.0f, Vector3(-0.49036676f, 0.835039f, 0.2495f)),
    WorldPoint(142.0f, Vector3(0.36461836f, 0.8979372f, 0.2465f)),
    WorldPoint(545.0f, Vector3(0.9351826f, 0.2571795f, 0.2435f)),
    WorldPoint(3162.0f, Vector3(-0.17452568f, 0.9545777f, 0.2415f)),
    WorldPoint(131.0f, Vector3(-0.9666741f, -0.090448946f, 0.2395f)),
    WorldPoint(337.0f, Vector3(0.6518632f, 0.7198556f, 0.2385f)),
    WorldPoint(2820.0f, Vector3(0.005593265f, -0.9713714f, 0.2375f)),
    WorldPoint(341.0f, Vector3(-0.6604421f, 0.7126598f, 0.2365f)),
    WorldPoint(362.0f, Vector3(0.86095476f, 0.45346078f, 0.2305f)),
    WorldPoint(101.0f, Vector3(0.48363122f, 0.8457249f, 0.2255f)),
    WorldPoint(2586.0f, Vector3(0.21471521f, -0.9505247f, 0.2245f)),
    WorldPoint(450.0f, Vector3(-0.62413526f, -0.7492616f, 0.2215f)),
    WorldPoint(3432.0f, Vector3(-0.9750387f, 0.039461683f, 0.2185f)),
    WorldPoint(5109.0f, Vector3(0.7457899f, 0.6296755f, 0.2175f)),
    WorldPoint(637.0f, Vector3(-0.5623187f, 0.7983467f, 0.2155f)),
    WorldPoint(449.0f, Vector3(0.91818106f, 0.33623403f, 0.2095f)),
    WorldPoint(262.0f, Vector3(-0.25478503f, 0.9444725f, 0.2075f)),
    WorldPoint(946.0f, Vector3(-0.9635465f, -0.17131215f, 0.2055f)),
    WorldPoint(345.0f, Vector3(0.5948968f, 0.7773529f, 0.2045f)),
    WorldPoint(3479.0f, Vector3(0.08645435f, -0.9752504f, 0.2035f)),
    WorldPoint(174.0f, Vector3(-0.72267395f, 0.6608601f, 0.2025f)),
    WorldPoint(335.0f, Vector3(0.08480948f, 0.97622085f, 0.1995f)),
    WorldPoint(847.0f, Vector3(0.826842f, 0.52699155f, 0.1965f)),
    WorldPoint(17.0f, Vector3(0.4152195f, 0.8893371f, 0.1915f)),
    WorldPoint(2650.0f, Vector3(0.29462707f, -0.9364319f, 0.1905f)),
    WorldPoint(224.0f, Vector3(0.9589812f, 0.21171394f, 0.1885f)),
    WorldPoint(472.0f, Vector3(-0.56422216f, -0.80405045f, 0.1875f)),
    WorldPoint(2312.0f, Vector3(-0.12711306f, 0.9741971f, 0.1865f)),
    WorldPoint(57.0f, Vector3(-0.98195714f, -0.041472398f, 0.1845f)),
    WorldPoint(1094.0f, Vector3(0.69618297f, 0.69401515f, 0.1835f)),
    WorldPoint(1580.0f, Vector3(-0.04455163f, -0.982196f, 0.1825f)),
    WorldPoint(455.0f, Vector3(-0.6307316f, 0.7544769f, 0.1815f)),
    WorldPoint(370.0f, Vector3(0.8933381f, 0.41369894f, 0.1755f)),
    WorldPoint(122.0f, Vector3(0.53218925f, 0.8292794f, 0.1705f)),
    WorldPoint(3615.0f, Vector3(0.16777976f, -0.9711435f, 0.1695f)),
    WorldPoint(109.0f, Vector3(-0.77984923f, 0.60286224f, 0.1685f)),
    WorldPoint(4934.0f, Vector3(0.7854469f, 0.59721595f, 0.1625f)),
    WorldPoint(323.0f, Vector3(-0.52643555f, 0.83492833f, 0.1605f)),
    WorldPoint(187.0f, Vector3(0.3424635f, 0.9262357f, 0.1575f)),
    WorldPoint(102.0f, Vector3(0.94388205f, 0.2919185f, 0.1545f)),
    WorldPoint(1665.0f, Vector3(-0.20834666f, 0.9660929f, 0.1525f)),
    WorldPoint(907.0f, Vector3(0.64022255f, 0.7535017f, 0.1495f)),
    WorldPoint(2667.0f, Vector3(0.03690856f, -0.98822343f, 0.1485f)),
    WorldPoint(322.0f, Vector3(-0.6948557f, 0.7038603f, 0.1475f)),
    WorldPoint(1389.0f, Vector3(0.860878f, 0.48874003f, 0.1415f)),
    WorldPoint(1842.0f, Vector3(-0.30468935f, -0.9420319f, 0.1405f)),
    WorldPoint(433.0f, Vector3(-0.4117238f, 0.9005683f, 0.1395f)),
    WorldPoint(32.0f, Vector3(0.4643789f, 0.8750543f, 0.1365f)),
    WorldPoint(1973.0f, Vector3(0.24870661f, -0.9590541f, 0.1355f)),
    WorldPoint(10.0f, Vector3(-0.83133453f, 0.5392519f, 0.1345f)),
    WorldPoint(828.0f, Vector3(-0.077897176f, 0.98825085f, 0.1315f)),
    WorldPoint(1854.0f, Vector3(0.7371733f, 0.6633726f, 0.1285f)),
    WorldPoint(140.0f, Vector3(-0.095479354f, -0.9872322f, 0.1275f)),
    WorldPoint(126.0f, Vector3(-0.59653926f, 0.792552f, 0.1265f)),
    WorldPoint(228.0f, Vector3(0.9209363f, 0.37061584f, 0.1205f)),
    WorldPoint(122.0f, Vector3(0.57847095f, 0.8074844f, 0.1155f)),
    WorldPoint(3707.0f, Vector3(0.11891525f, -0.9862803f, 0.1145f)),
    WorldPoint(325.0f, Vector3(-0.7539943f, 0.647001f, 0.1135f)),
    WorldPoint(5000.0f, Vector3(0.8211098f, 0.5605555f, 0.1075f)),
    WorldPoint(736.0f, Vector3(-0.22683555f, -0.9680927f, 0.1065f)),
    WorldPoint(75.0f, Vector3(0.39215797f, 0.9141695f, 0.1025f)),
    WorldPoint(36.0f, Vector3(0.3283814f, -0.9390758f, 0.1015f)),
    WorldPoint(1584.0f, Vector3(-0.15985575f, 0.9823135f, 0.0975f)),
    WorldPoint(410.0f, Vector3(0.68250066f, 0.72475004f, 0.0945f)),
    WorldPoint(2401.0f, Vector3(-0.013694448f, -0.9955251f, 0.0935f)),
    WorldPoint(426.0f, Vector3(-0.66243184f, 0.7433894f, 0.0925f)),
    WorldPoint(45.0f, Vector3(-0.958363f, 0.2718163f, 0.0875f)),
    WorldPoint(70.0f, Vector3(0.8903544f, 0.4469752f, 0.0865f)),
    WorldPoint(27.0f, Vector3(-0.36748716f, 0.9261819f, 0.0845f)),
    WorldPoint(75.0f, Vector3(0.5115534f, 0.8553776f, 0.0815f)),
    WorldPoint(3023.0f, Vector3(0.20061204f, -0.9763578f, 0.0805f)),
    WorldPoint(251.0f, Vector3(-0.8075103f, 0.5844714f, 0.0795f)),
    WorldPoint(1088.0f, Vector3(0.7744235f, 0.62838364f, 0.0735f)),
    WorldPoint(150.0f, Vector3(-0.14657949f, -0.98653847f, 0.0725f)),
    WorldPoint(1.0f, Vector3(0.3162656f, 0.9461944f, 0.0685f)),
    WorldPoint(1316.0f, Vector3(0.62197846f, 0.78069365f, 0.0605f)),
    WorldPoint(3314.0f, Vector3(0.06872755f, -0.99585956f, 0.0595f)),
    WorldPoint(292.0f, Vector3(-0.7234139f, 0.6879318f, 0.0585f)),
    WorldPoint(5173.0f, Vector3(0.85243213f, 0.52019536f, 0.0525f)),
    WorldPoint(305.0f, Vector3(-0.2771845f, -0.9594355f, 0.0515f)),
    WorldPoint(66.0f, Vector3(0.440151f, 0.8966665f, 0.0475f)),
    WorldPoint(2920.0f, Vector3(0.28114912f, -0.95853686f, 0.0465f)),
    WorldPoint(65.0f, Vector3(-0.85483235f, 0.51690555f, 0.0455f)),
    WorldPoint(1326.0f, Vector3(0.7212859f, 0.6915102f, 0.0395f)),
    WorldPoint(1712.0f, Vector3(-0.064747855f, -0.9971587f, 0.0385f)),
    WorldPoint(228.0f, Vector3(-0.6258518f, 0.77904f, 0.0375f)),
    WorldPoint(23.0f, Vector3(0.91499406f, 0.40223572f, 0.0315f)),
    WorldPoint(191.0f, Vector3(0.55622065f, 0.83061206f, 0.0265f)),
    WorldPoint(3967.0f, Vector3(0.15093453f, -0.9882148f, 0.0255f)),
    WorldPoint(149.0f, Vector3(-0.7788434f, 0.62673974f, 0.0245f)),
    WorldPoint(4948.0f, Vector3(0.80754733f, 0.5895126f, 0.0185f)),
    WorldPoint(1859.0f, Vector3(-0.19725378f, -0.98019624f, 0.0175f)),
    WorldPoint(317.0f, Vector3(0.3649936f, 0.93091214f, 0.0135f)),
    WorldPoint(1990.0f, Vector3(0.3596919f, -0.9329874f, 0.0125f)),
    WorldPoint(422.0f, Vector3(-0.19169194f, 0.9814183f, 0.0085f)),
    WorldPoint(2013.0f, Vector3(0.66223544f, 0.7492756f, 0.0055f)),
    WorldPoint(2965.0f, Vector3(0.017816737f, -0.99983114f, 0.0045f)),
    WorldPoint(428.0f, Vector3(-0.6885165f, 0.7252122f, 0.0035f)),
    WorldPoint(1293.0f, Vector3(-0.97018605f, 0.24235676f, -0.0015f)),
    WorldPoint(5224.0f, Vector3(0.8790929f, 0.47664398f, -0.0025f)),
    WorldPoint(457.0f, Vector3(-0.3979608f, 0.91739136f, -0.0045f)),
    WorldPoint(260.0f, Vector3(0.48589894f, 0.87398285f, -0.0075f)),
    WorldPoint(3698.0f, Vector3(0.23207831f, -0.97265995f, -0.0085f)),
    WorldPoint(47.0f, Vector3(-0.8281422f, 0.5604375f, -0.0095f)),
    WorldPoint(793.0f, Vector3(0.7561555f, 0.65420836f, -0.0155f)),
    WorldPoint(2001.0f, Vector3(-0.11565227f, -0.99315274f, -0.0165f)),
    WorldPoint(242.0f, Vector3(-0.58557665f, 0.8104281f, -0.0175f)),
    WorldPoint(33.0f, Vector3(0.2868524f, 0.95775545f, -0.0205f)),
    WorldPoint(328.0f, Vector3(0.5978764f, 0.8010815f, -0.0285f)),
    WorldPoint(3234.0f, Vector3(0.10026441f, -0.9945234f, -0.0295f)),
    WorldPoint(247.0f, Vector3(-0.74570036f, 0.665583f, -0.0305f)),
    WorldPoint(4963.0f, Vector3(0.8361823f, 0.5472357f, -0.0365f)),
    WorldPoint(494.0f, Vector3(-0.2469132f, -0.9683117f, -0.0375f)),
    WorldPoint(69.0f, Vector3(-0.4720011f, 0.8807569f, -0.0385f)),
    WorldPoint(706.0f, Vector3(0.411736f, 0.9103577f, -0.0415f)),
    WorldPoint(3123.0f, Vector3(0.31132337f, -0.9493532f, -0.0425f)),
    WorldPoint(2410.0f, Vector3(0.69878536f, 0.71361667f, -0.0495f)),
    WorldPoint(1700.0f, Vector3(-0.033219747f, -0.99817145f, -0.0505f)),
    WorldPoint(334.0f, Vector3(-0.6497267f, 0.75842136f, -0.0515f)),
    WorldPoint(30.0f, Vector3(0.20653188f, 0.97692084f, -0.0545f)),
    WorldPoint(677.0f, Vector3(-0.9550177f, 0.29111668f, -0.0565f)),
    WorldPoint(1512.0f, Vector3(0.90079534f, 0.4304202f, -0.0575f)),
    WorldPoint(320.0f, Vector3(0.52887297f, 0.84639657f, -0.0625f)),
    WorldPoint(3386.0f, Vector3(0.1817467f, -0.98129296f, -0.0635f)),
    WorldPoint(312.0f, Vector3(-0.796817f, 0.60076815f, -0.0645f)),
    WorldPoint(3156.0f, Vector3(0.7867078f, 0.6132867f, -0.0705f)),
    WorldPoint(1755.0f, Vector3(-0.16581286f, -0.9835618f, -0.0715f)),
    WorldPoint(286.0f, Vector3(-0.54208195f, 0.83719224f, -0.0725f)),
    WorldPoint(382.0f, Vector3(0.334498f, 0.93936723f, -0.0755f)),
    WorldPoint(997.0f, Vector3(0.3878554f, -0.9185401f, -0.0765f)),
    WorldPoint(357.0f, Vector3(0.94878364f, 0.30601856f, -0.0785f)),
    WorldPoint(45.0f, Vector3(-0.22186324f, 0.9717492f, -0.0805f)),
    WorldPoint(1947.0f, Vector3(0.6360331f, 0.7671308f, -0.0835f)),
    WorldPoint(2573.0f, Vector3(0.049194206f, -0.9952084f, -0.0845f)),
    WorldPoint(497.0f, Vector3(-0.7084672f, 0.7005455f, -0.0855f)),
    WorldPoint(5009.0f, Vector3(0.8599881f, 0.5020441f, -0.0915f)),
    WorldPoint(119.0f, Vector3(-0.4248574f, 0.90041876f, -0.0935f)),
    WorldPoint(362.0f, Vector3(0.4559421f, 0.8847624f, -0.0965f)),
    WorldPoint(3059.0f, Vector3(0.26142532f, -0.9602867f, -0.0975f)),
    WorldPoint(168.0f, Vector3(-0.841348f, 0.5314427f, -0.0985f)),
    WorldPoint(1362.0f, Vector3(0.73118967f, 0.6741227f, -0.1045f)),
    WorldPoint(326.0f, Vector3(-0.083784305f, -0.9908834f, -0.1055f)),
    WorldPoint(252.0f, Vector3(-0.6074871f, 0.7871577f, -0.1065f)),
    WorldPoint(1959.0f, Vector3(-0.93454444f, 0.3379267f, -0.1115f)),
    WorldPoint(1009.0f, Vector3(0.91726613f, 0.38205576f, -0.1125f)),
    WorldPoint(323.0f, Vector3(-0.30033115f, 0.9469377f, -0.1145f)),
    WorldPoint(440.0f, Vector3(0.5685567f, 0.81420946f, -0.1175f)),
    WorldPoint(2697.0f, Vector3(0.1307389f, -0.98430943f, -0.1185f)),
    WorldPoint(550.0f, Vector3(-0.7612024f, 0.6374093f, -0.1195f)),
    WorldPoint(4518.0f, Vector3(0.8125607f, 0.5692055f, -0.1255f)),
    WorldPoint(1477.0f, Vector3(-0.21463668f, -0.9684673f, -0.1265f)),
    WorldPoint(347.0f, Vector3(-0.49585983f, 0.85899174f, -0.1275f)),
    WorldPoint(514.0f, Vector3(0.37984657f, 0.9157982f, -0.1305f)),
    WorldPoint(1852.0f, Vector3(0.33848062f, -0.93173957f, -0.1315f)),
    WorldPoint(137.0f, Vector3(-0.878848f, 0.4583338f, -0.1325f)),
    WorldPoint(2217.0f, Vector3(0.6702173f, 0.7291272f, -0.1385f)),
    WorldPoint(52.0f, Vector3(-0.0016787483f, -0.99022067f, -0.1395f)),
    WorldPoint(663.0f, Vector3(-0.6675512f, 0.73118746f, -0.1405f)),
    WorldPoint(3466.0f, Vector3(0.8786451f, 0.4544452f, -0.1465f)),
    WorldPoint(538.0f, Vector3(-0.37577564f, 0.91473514f, -0.1485f)),
    WorldPoint(395.0f, Vector3(0.49706912f, 0.8543828f, -0.1515f)),
    WorldPoint(3229.0f, Vector3(0.21057123f, -0.96561044f, -0.1525f)),
    WorldPoint(718.0f, Vector3(-0.8074027f, 0.569683f, -0.1535f)),
    WorldPoint(1592.0f, Vector3(0.7590257f, 0.6312209f, -0.1595f)),
    WorldPoint(1100.0f, Vector3(-0.13327646f, -0.97799647f, -0.1605f)),
    WorldPoint(539.0f, Vector3(-0.56226027f, 0.81103706f, -0.1615f)),
    WorldPoint(197.0f, Vector3(0.3013872f, 0.93920475f, -0.1645f)),
    WorldPoint(1259.0f, Vector3(-0.90895015f, 0.38221374f, -0.1665f)),
    WorldPoint(674.0f, Vector3(0.92825377f, 0.33209744f, -0.1675f)),
    WorldPoint(36.0f, Vector3(-0.249586f, 0.95340264f, -0.1695f)),
    WorldPoint(726.0f, Vector3(0.6044437f, 0.77774775f, -0.1725f)),
    WorldPoint(1946.0f, Vector3(0.07964886f, -0.9816078f, -0.1735f)),
    WorldPoint(1368.0f, Vector3(-0.7216676f, 0.6698848f, -0.1745f)),
    WorldPoint(427.0f, Vector3(0.98445815f, -0.0064715496f, -0.1755f)),
    WorldPoint(3402.0f, Vector3(0.83334893f, 0.5224455f, -0.1805f)),
    WorldPoint(197.0f, Vector3(-0.44742095f, 0.87550455f, -0.1825f)),
    WorldPoint(337.0f, Vector3(0.42233142f, 0.8872575f, -0.1855f)),
    WorldPoint(2471.0f, Vector3(0.28786433f, -0.9393359f, -0.1865f)),
    WorldPoint(675.0f, Vector3(-0.8466104f, 0.49809092f, -0.1875f)),
    WorldPoint(1541.0f, Vector3(0.69996697f, 0.68746203f, -0.1935f)),
    WorldPoint(855.0f, Vector3(-0.62338257f, 0.7570825f, -0.1955f)),
    WorldPoint(1559.0f, Vector3(0.8918523f, 0.40496564f, -0.2015f)),
    WorldPoint(390.0f, Vector3(-0.3253068f, 0.9234518f, -0.2035f)),
    WorldPoint(746.0f, Vector3(0.53457946f, 0.8195014f, -0.2065f)),
    WorldPoint(2610.0f, Vector3(0.15934853f, -0.9651693f, -0.2075f)),
    WorldPoint(4042.0f, Vector3(-0.7692939f, 0.6039161f, -0.2085f)),
    WorldPoint(133.0f, Vector3(0.97497976f, 0.07432536f, -0.2095f)),
    WorldPoint(1890.0f, Vector3(0.78188354f, 0.58536136f, -0.2145f)),
    WorldPoint(573.0f, Vector3(-0.51453066f, 0.8296903f, -0.2165f)),
    WorldPoint(170.0f, Vector3(0.3451453f, 0.91252095f, -0.2195f)),
    WorldPoint(1513.0f, Vector3(-0.8784451f, 0.42340523f, -0.2215f)),
    WorldPoint(148.0f, Vector3(0.93352616f, 0.28110963f, -0.2225f)),
    WorldPoint(1370.0f, Vector3(0.6360341f, 0.73736316f, -0.2275f)),
    WorldPoint(1565.0f, Vector3(-0.67860675f, 0.6977267f, -0.2295f)),
    WorldPoint(43.0f, Vector3(0.9714552f, -0.05607608f, -0.2305f)),
    WorldPoint(1222.0f, Vector3(0.8487219f, 0.4735091f, -0.2355f)),
    WorldPoint(314.0f, Vector3(-0.39729682f, 0.88642484f, -0.2375f)),
    WorldPoint(413.0f, Vector3(0.4613856f, 0.8539807f, -0.2405f)),
    WorldPoint(2553.0f, Vector3(0.23658377f, -0.9411195f, -0.2415f)),
    WorldPoint(1319.0f, Vector3(-0.80995834f, 0.53400487f, -0.2425f)),
    WorldPoint(1431.0f, Vector3(0.7248282f, 0.64255106f, -0.2485f)),
    WorldPoint(960.0f, Vector3(-0.5764153f, 0.7778143f, -0.2505f)),
    WorldPoint(369.0f, Vector3(0.26634547f, 0.92994505f, -0.2535f)),
    WorldPoint(5.0f, Vector3(0.89932394f, 0.35415286f, -0.2565f)),
    WorldPoint(149.0f, Vector3(-0.2740216f, 0.9263314f, -0.2585f)),
    WorldPoint(805.0f, Vector3(0.5679366f, 0.7804266f, -0.2615f)),
    WorldPoint(2245.0f, Vector3(0.10836135f, -0.9588282f, -0.2625f)),
    WorldPoint(2197.0f, Vector3(-0.7273759f, 0.633634f, -0.2635f)),
    WorldPoint(439.0f, Vector3(0.7993627f, 0.5370186f, -0.2695f)),
    WorldPoint(922.0f, Vector3(-0.46480614f, 0.8427592f, -0.2715f)),
    WorldPoint(186.0f, Vector3(0.38566673f, 0.88085806f, -0.2745f)),
    WorldPoint(1325.0f, Vector3(-0.8432651f, 0.46092477f, -0.2765f)),
    WorldPoint(143.0f, Vector3(0.93286777f, 0.22967692f, -0.2775f)),
    WorldPoint(919.0f, Vector3(0.6628304f, 0.6934332f, -0.2825f)),
    WorldPoint(1345.0f, Vector3(-0.6324396f, 0.720472f, -0.2845f)),
    WorldPoint(13.0f, Vector3(0.18679167f, 0.93938947f, -0.2875f)),
    WorldPoint(480.0f, Vector3(0.85833913f, 0.4229228f, -0.2905f)),
    WorldPoint(1120.0f, Vector3(-0.34604198f, 0.89145875f, -0.2925f)),
    WorldPoint(643.0f, Vector3(0.49643743f, 0.8162289f, -0.2955f)),
    WorldPoint(1354.0f, Vector3(0.18523708f, -0.93689644f, -0.2965f)),
    WorldPoint(2327.0f, Vector3(-0.76920193f, 0.5655282f, -0.2975f)),
    WorldPoint(1243.0f, Vector3(0.7443505f, 0.59483624f, -0.3035f)),
    WorldPoint(1478.0f, Vector3(-0.5271293f, 0.7929719f, -0.3055f)),
    WorldPoint(1095.0f, Vector3(0.30826357f, 0.8998896f, -0.3085f)),
    WorldPoint(476.0f, Vector3(-0.2225136f, 0.92314976f, -0.3135f)),
    WorldPoint(706.0f, Vector3(0.5966002f, 0.73749304f, -0.3165f)),
    WorldPoint(1691.0f, Vector3(-0.6820292f, 0.6583266f, -0.3185f)),
    WorldPoint(35.0f, Vector3(0.81106687f, 0.48669323f, -0.3245f)),
    WorldPoint(1695.0f, Vector3(-0.41362032f, 0.8498917f, -0.3265f)),
    WorldPoint(1017.0f, Vector3(0.42234612f, 0.84442496f, -0.3295f)),
    WorldPoint(161.0f, Vector3(-0.8036712f, 0.49418658f, -0.3315f)),
    WorldPoint(95.0f, Vector3(0.68433166f, 0.64636207f, -0.3375f)),
    WorldPoint(1050.0f, Vector3(-0.58361256f, 0.7376558f, -0.3395f)),
    WorldPoint(802.0f, Vector3(0.2300452f, 0.9109188f, -0.3425f)),
    WorldPoint(17.0f, Vector3(0.8618648f, 0.3712395f, -0.3455f)),
    WorldPoint(906.0f, Vector3(-0.29423708f, 0.8903192f, -0.3475f)),
    WorldPoint(466.0f, Vector3(0.52690417f, 0.7742879f, -0.3505f)),
    WorldPoint(1093.0f, Vector3(-0.7246767f, 0.5921042f, -0.3525f)),
    WorldPoint(258.0f, Vector3(0.028937723f, 0.93422824f, -0.3555f)),
    WorldPoint(36.0f, Vector3(0.75808f, 0.54478663f, -0.3585f)),
    WorldPoint(997.0f, Vector3(-0.47603175f, 0.8021431f, -0.3605f)),
    WorldPoint(468.0f, Vector3(0.34651178f, 0.86475277f, -0.3635f)),
    WorldPoint(129.0f, Vector3(-0.17140333f, 0.91368955f, -0.3685f)),
    WorldPoint(232.0f, Vector3(0.74325836f, -0.5577067f, -0.3695f)),
    WorldPoint(262.0f, Vector3(0.62001836f, 0.6910607f, -0.3715f)),
    WorldPoint(1323.0f, Vector3(-0.6336608f, 0.67747456f, -0.3735f)),
    WorldPoint(303.0f, Vector3(0.1519015f, 0.9138784f, -0.3765f)),
    WorldPoint(124.0f, Vector3(0.8165962f, 0.43491417f, -0.3795f)),
    WorldPoint(5.0f, Vector3(0.84087205f, -0.38292035f, -0.3825f)),
    WorldPoint(582.0f, Vector3(0.45455906f, 0.80345243f, -0.3845f)),
    WorldPoint(62.0f, Vector3(-0.047590412f, 0.9197961f, -0.3895f)),
    WorldPoint(77.0f, Vector3(0.700025f, 0.59658086f, -0.3925f)),
    WorldPoint(722.0f, Vector3(0.269816f, 0.8770365f, -0.3975f)),
    WorldPoint(1620.0f, Vector3(-0.24249335f, 0.8827178f, -0.4025f)),
    WorldPoint(315.0f, Vector3(0.77470154f, -0.48685244f, -0.4035f)),
    WorldPoint(141.0f, Vector3(0.5521828f, 0.72846687f, -0.4055f)),
    WorldPoint(27.0f, Vector3(-0.67674315f, 0.61315775f, -0.4075f)),
    WorldPoint(1272.0f, Vector3(0.9127446f, 0.0050075403f, -0.4085f)),
    WorldPoint(367.0f, Vector3(0.07473541f, 0.9087928f, -0.4105f)),
    WorldPoint(763.0f, Vector3(0.7655501f, 0.49290046f, -0.4135f)),
    WorldPoint(4.0f, Vector3(0.6376175f, 0.64151514f, -0.4265f)),
    WorldPoint(663.0f, Vector3(0.19316554f, 0.88118947f, -0.4315f)),
    WorldPoint(555.0f, Vector3(0.79826176f, -0.4139709f, -0.4375f)),
    WorldPoint(1224.0f, Vector3(0.4816512f, 0.75818986f, -0.4395f)),
    WorldPoint(348.0f, Vector3(0.893287f, 0.07894368f, -0.4425f)),
    WorldPoint(105.0f, Vector3(-0.19145848f, 0.86835325f, -0.4575f)),
    WorldPoint(327.0f, Vector3(0.72731805f, -0.5106723f, -0.4585f)),
    WorldPoint(941.0f, Vector3(0.5716369f, 0.67909575f, -0.4605f)),
    WorldPoint(193.0f, Vector3(0.11748524f, 0.87721545f, -0.4655f)),
    WorldPoint(178.0f, Vector3(0.40929836f, 0.7799183f, -0.4735f)),
    WorldPoint(459.0f, Vector3(0.7511337f, -0.4395929f, -0.4925f)),
    WorldPoint(817.0f, Vector3(0.5029215f, 0.70890033f, -0.4945f)),
    WorldPoint(97.0f, Vector3(0.6761756f, -0.5283032f, -0.5135f)),
    WorldPoint(289.0f, Vector3(0.7667039f, -0.36737293f, -0.5265f)),
    WorldPoint(463.0f, Vector3(0.6996899f, -0.45899647f, -0.5475f)),
    WorldPoint(253.0f, Vector3(0.7738418f, -0.2949723f, -0.5605f)),
    WorldPoint(112.0f, Vector3(0.59022856f, 0.57110417f, -0.5705f)),
    WorldPoint(386.0f, Vector3(0.714763f, -0.3885507f, -0.5815f)),
    WorldPoint(422.0f, Vector3(0.44875962f, 0.6768623f, -0.5835f)),
    WorldPoint(268.0f, Vector3(0.644117f, -0.47128236f, -0.6025f)),
    WorldPoint(797.0f, Vector3(0.524793f, 0.5993096f, -0.6045f)),
    WorldPoint(376.0f, Vector3(0.7211533f, -0.3179586f, -0.6155f)),
    WorldPoint(546.0f, Vector3(0.6579317f, -0.40248433f, -0.6365f)),
    WorldPoint(218.0f, Vector3(0.47578958f, -0.5985349f, -0.6445f)),
    WorldPoint(163.0f, Vector3(0.7186902f, -0.24826212f, -0.6495f)),
    WorldPoint(27.0f, Vector3(0.5845588f, -0.47537854f, -0.6575f)),
    WorldPoint(223.0f, Vector3(0.6626852f, -0.3335836f, -0.6705f)),
    WorldPoint(62.0f, Vector3(0.50303715f, -0.5353423f, -0.6785f)),
    WorldPoint(117.0f, Vector3(0.5961644f, -0.40794086f, -0.6915f)),
    WorldPoint(793.0f, Vector3(0.71715325f, -0.024062473f, -0.6965f)),
    WorldPoint(808.0f, Vector3(0.41554752f, -0.58139485f, -0.6995f)),
    WorldPoint(130.0f, Vector3(0.5210674f, -0.4699282f, -0.7125f)),
    WorldPoint(113.0f, Vector3(0.5981274f, -0.34043112f, -0.7255f)),
    WorldPoint(405.0f, Vector3(0.09332484f, -0.67865914f, -0.7285f)),
    WorldPoint(554.0f, Vector3(0.43953493f, -0.5184465f, -0.7335f)),
    WorldPoint(255.0f, Vector3(0.52922666f, -0.4033074f, -0.7465f)),
    WorldPoint(46.0f, Vector3(0.6573721f, -0.0557642f, -0.7515f)),
    WorldPoint(37.0f, Vector3(0.5899869f, -0.27399856f, -0.7595f)),
    WorldPoint(256.0f, Vector3(0.45350394f, -0.45307606f, -0.7675f)),
    WorldPoint(39.0f, Vector3(0.6199341f, -0.1375698f, -0.7725f)),
    WorldPoint(316.0f, Vector3(0.52681744f, -0.33657563f, -0.7805f)),
    WorldPoint(684.0f, Vector3(0.053107813f, -0.61911815f, -0.7835f)),
    WorldPoint(74.0f, Vector3(0.4564971f, -0.3862747f, -0.8015f)),
    WorldPoint(28.0f, Vector3(0.5857978f, -0.07999181f, -0.8065f)),
    WorldPoint(491.0f, Vector3(0.5412054f, -0.1494672f, -0.8275f)),
)

private data class City(
    val name: String,
    val coordinates: Vector3,
)

private val CitiesList = listOf(
    City(
        name = "Prague",
        coordinates = Vector3(0.1600f, 0.7669f, 0.6215f),
    ),
    City(
        name = "Tokyo",
        coordinates = Vector3(0.5259f, 0.5832f, -0.6191f),
    ),
    City(
        name = "New York City",
        coordinates = Vector3(-0.7286f, 0.6523f, 0.2089f),
    ),
    City(
        name = "Dubai",
        coordinates = Vector3(0.7436f, 0.4259f, 0.5155f),
    ),
    City(
        name = "Bangkok",
        coordinates = Vector3(0.9550f, 0.2378f, -0.1770f),
    ),
    City(
        name = "Sydney",
        coordinates = Vector3(0.3999f, -0.5573f, -0.7277f),
    ),
    City(
        name = "Rio de Janeiro",
        coordinates = Vector3(-0.6302f, -0.3892f, 0.6718f),
    ),
    City(
        name = "Cape Town",
        coordinates = Vector3(0.2622f, -0.5581f, 0.7872f),
    )
)
