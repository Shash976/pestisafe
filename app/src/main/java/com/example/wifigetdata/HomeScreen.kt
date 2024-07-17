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




