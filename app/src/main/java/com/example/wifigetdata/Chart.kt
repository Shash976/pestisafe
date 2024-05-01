package com.example.wifigetdata

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer

@Composable
fun ChartMaking(){
    val model = remember {
        CartesianChartModelProducer.build()
    }
    model.tryRunTransaction {

    }
    CartesianChartHost(chart = rememberCartesianChart(rememberLineCartesianLayer()), modelProducer = model)
}

@Preview(showBackground = true)
@Composable
fun ChartMakingPreview(){
    ChartMaking()
}