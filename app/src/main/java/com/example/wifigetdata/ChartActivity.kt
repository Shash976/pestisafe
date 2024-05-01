package com.example.wifigetdata

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineSpec
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.shader.color
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.core.cartesian.data.AxisValueOverrider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.core.common.shader.DynamicShader

private val model1 = CartesianChartModel(LineCartesianLayerModel.build { series(0, 2, 4.5, 0.123, 2) })

private val model2 =
    CartesianChartModel(
        LineCartesianLayerModel.build { series(0.0, 2.5, 4.8, 0.1, 2.78) },
        LineCartesianLayerModel.build { series(1, 3, 4, 1, 3) },
    )

private val model3 =
    CartesianChartModel(
        LineCartesianLayerModel.build {
            series(3, 2, 2, 3, 1)
            series(1, 3, 1, 2, 3)
        },
    )

@Preview("Line Chart Dark", widthDp = 200, showBackground = true)
@Composable
fun LineChartDark() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.Black,
    ) {
        val yellow = Color(0xFFFFAA4A)
        val pink = Color(0xFFFF4AAA)

        val x = model1.models
        val y = x[0]


        CartesianChartHost(
            modifier = Modifier.padding(8.dp),
            chart =
            rememberCartesianChart(
                rememberLineCartesianLayer(
                    listOf(
                        rememberLineSpec(
                            shader = DynamicShader.color(yellow),
                            backgroundShader =
                            DynamicShader.verticalGradient(
                                arrayOf(yellow.copy(alpha = 0.5f), yellow.copy(alpha = 0f)),
                            ),
                        ),
                        rememberLineSpec(
                            shader = DynamicShader.color(pink),
                            backgroundShader =
                            DynamicShader.verticalGradient(
                                arrayOf(pink.copy(alpha = 0.5f), pink.copy(alpha = 0f)),
                            ),
                        ),
                    ),
                    axisValueOverrider = AxisValueOverrider.fixed(maxY = 4f),
                ),
            ),
            model = model3,
        )
    }
}

@Preview("Line Chart", widthDp = 200, showBackground = true)
@Composable
fun RegularLineChart() {
    CartesianChartHost(
        chart = rememberCartesianChart(rememberLineCartesianLayer(), startAxis = rememberStartAxis()),
        model = model1,
    )
}

@Preview("Line Chart Expanded", widthDp = 200, showBackground = true)
@Composable
fun RegularLineChartExpanded() {
    CartesianChartHost(
        chart =
        rememberCartesianChart(
            rememberLineCartesianLayer(axisValueOverrider = AxisValueOverrider.fixed(minY = -1f, maxY = 5f)),
            startAxis = rememberStartAxis(),
        ),
        model = model1,
    )
}

@Preview("Line Chart Collapsed", widthDp = 200, showBackground = true)
@Composable
fun RegularLineChartCollapsed() {
    CartesianChartHost(
        chart =
        rememberCartesianChart(
            rememberLineCartesianLayer(axisValueOverrider = AxisValueOverrider.fixed(minY = 1f, maxY = 3f)),
            startAxis = rememberStartAxis(),
        ),
        model = model1,
    )
}

@Preview("Composed Chart", widthDp = 200, showBackground = true)
@Composable
fun ComposedLineChart() {
    CartesianChartHost(
        chart =
        rememberCartesianChart(
            rememberLineCartesianLayer(),
            rememberLineCartesianLayer(
                listOf(
                    rememberLineSpec(
                        shader = DynamicShader.color(Color.Blue),
                        backgroundShader =
                        DynamicShader.verticalGradient(
                            arrayOf(Color.Blue.copy(alpha = 0.4f), Color.Blue.copy(alpha = 0f)),
                        ),
                    ),
                ),
            ),
            startAxis = rememberStartAxis(),
        ),
        model = model2,
    )
}

@Preview("Composed Chart Collapsed", widthDp = 200, showBackground = true)
@Composable
fun ComposedLineChartCollapsed() {
    CartesianChartHost(
        chart =
        rememberCartesianChart(
            rememberLineCartesianLayer(axisValueOverrider = AxisValueOverrider.fixed(minY = 1f, maxY = 3f)),
            rememberLineCartesianLayer(axisValueOverrider = AxisValueOverrider.fixed(minY = 1f, maxY = 3f)),
            startAxis = rememberStartAxis(),
        ),
        model = model2,
    )
}