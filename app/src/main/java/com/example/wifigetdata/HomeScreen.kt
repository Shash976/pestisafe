package com.example.wifigetdata

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(sharedViewModel: MainViewModel, navController: NavController, context: MainActivity){
    //TODO HomeScreen
    val modelProducer1 = remember{CartesianChartModelProducer.build()}
    val modelProducer2 = remember { CartesianChartModelProducer.build()   }
    val chartCoroutineScope = rememberCoroutineScope()
    val updateCoroutineScope = rememberCoroutineScope()

    val allData = remember { mutableStateOf(emptyList<DataValue>())    }
    LaunchedEffect(Unit) {
        updateCoroutineScope.launch{
            sharedViewModel.theValue.collect {
                if (allData.value.size >= 2){
                    val low = sharedViewModel.allData.value[0].voltage
                    val high = sharedViewModel.allData.value[1].voltage
                    if (it > low && it < high){
                        sharedViewModel.updateConcentration()
                    }
                }
                else {
                    sharedViewModel.updateConcentration()
                }
            }
        }
        chartCoroutineScope.launch {
            sharedViewModel.allData.collect { data ->
                println("collecting.....")
                allData.value = data
                println(allData.value.size)
                withContext(Dispatchers.Default) {
                    modelProducer1.tryRunTransaction {
                        lineSeries {
                            series(
                                y = allData.value.map { it.concentration },
                            )
                            series(
                                y = allData.value.map { it.voltage },
                            )
                        }
                    }
                    println("\t Added 1")
                    modelProducer2.tryRunTransaction {
                        lineSeries {
                            series(
                                x = allData.value.sortedBy { it.concentration }
                                    .map { it.concentration },
                                y = allData.value.sortedBy { it.concentration }.map { it.voltage },
                            )
                        }
                    }
                    println("\t Added 2")
                }
                    println("Added series")
            }
        }
    }

    Column (modifier = Modifier.verticalScroll(rememberScrollState())){
        Text("Home Screen!")
        MainChart(modelProducer1, modifier = Modifier.padding(10.dp), chartColors = listOf(Color.Black, Color.Cyan))
//        CartesianChartHost(
//            rememberCartesianChart(
//                rememberLineCartesianLayer(),
//                startAxis = rememberStartAxis(),
//                bottomAxis = rememberBottomAxis()
//            ),
//            modelProducer = modelProducer,
//        )
        MainChart(modelProducer2, modifier = Modifier.padding(15.dp), chartColors = listOf(Color.Black))

        Card(modifier = Modifier.padding(10.dp)){
            if (allData.value.isNotEmpty()){ Text("Voltage: ${allData.value.last().voltage} V \nConcentration: ${allData.value.last().concentration} μm") }
        }

        val formatButtonText = remember{ mutableStateOf("Choose Format") }
        val openDialog = remember { mutableStateOf(false)  }
        val selected = remember { mutableStateOf("") }
        val chosenFormat = remember { mutableStateOf("")   }
        val options = listOf("CSV", "JSON", "Excel")
        if (openDialog.value){
            Dialog(onDismissRequest = { openDialog.value = false}) {
                Card (modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(950.dp)
                    .wrapContentSize(Alignment.Center), shape = RoundedCornerShape(20.dp)
                ){
                    Text(text="Formats", modifier = Modifier
                        .padding(10.dp)
                        .padding(top = 2.dp, start = 4.dp),textAlign = TextAlign.Center, fontSize = 20.sp)

                    Column {
                        options.forEach {
                            Row(modifier = Modifier.padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = (it==selected.value), onClick = { selected.value = it })
                                Text(text = it, modifier = Modifier
                                    .padding(1.dp)
                                    .wrapContentSize(Alignment.Center), textAlign = TextAlign.Center)
                            }
                        }
                    }
                    Row (horizontalArrangement = Arrangement.End, modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp)){
                        TextButton(onClick = { openDialog.value = false;}, shape= RoundedCornerShape(4.dp)) {
                            Text(text = "Cancel")
                        }
                        TextButton(enabled = (selected.value in options), onClick = { chosenFormat.value = selected.value ; openDialog.value=false; formatButtonText.value=chosenFormat.value}) {
                            Text(text = "Set Format")
                        }
                    }
                }
            }
        }
        Row {
            Button(enabled = (formatButtonText.value in options), onClick = {
                when (chosenFormat.value) {
                    in listOf("CSV","JSON") -> downloadFile(context,"data.${chosenFormat.value.lowercase()}", allData.value.toTypedArray(), chosenFormat.value)
                    "Excel" -> Toast.makeText(context, "Not available yet", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Save data as ")
            }
            Button(onClick = { openDialog.value = true}) {
                Text(formatButtonText.value)
            }
        }

    }
}




