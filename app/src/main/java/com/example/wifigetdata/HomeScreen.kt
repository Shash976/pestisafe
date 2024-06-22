package com.example.wifigetdata

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(sharedViewModel: MainViewModel, navController: NavController, context: MainActivity){
    //TODO HomeScreen
    val allData = sharedViewModel.allData.collectAsState()
    val modelProducer = remember{CartesianChartModelProducer.build()}
    val chartCoroutineScope = rememberCoroutineScope()
    MainChart(modelProducer, modifier = Modifier.padding(10.dp), chartColors = listOf(Color.Black))
    LaunchedEffect(allData.value) {
        withContext(Dispatchers.Default) {
            val deferred = modelProducer.runTransaction {
                lineSeries {
                    series(
                        x = allData.value.map { it.concentration },
                        y = allData.value.map { it.voltage }
                    )
                }
            }
        }
    }
}


