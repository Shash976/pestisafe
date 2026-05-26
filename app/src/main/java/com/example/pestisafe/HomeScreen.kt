package com.example.pestisafe

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
/**
 * The home screen of the app
 * @param sharedViewModel the view model to use
 * @param navController the navigation controller
 * @param context the context to use
 * @see MainViewModel
 * @see NavController
 * @see MainActivity
 * @see CartesianChartModelProducer
 * @see DataValue
 * @see Pesticide
 * @see Commodity
 */
fun HomeScreen(sharedViewModel: MainViewModel, navController: NavController, context: MainActivity){
    val modelProducer1 = remember{CartesianChartModelProducer.build()}
    val modelProducer2 = remember { CartesianChartModelProducer.build()   }
    val chartCoroutineScope = rememberCoroutineScope()
    val updateCoroutineScope = rememberCoroutineScope()

    val allData = remember { mutableStateOf(emptyList<DataValue>())    }
    val liveVoltage = remember { mutableStateOf<Double?>(null) }
    val liveConcentration = remember { mutableStateOf<Double?>(null) }
    LaunchedEffect(Unit) {
        updateCoroutineScope.launch{
            sharedViewModel.theValue.collect { received ->
                // Only update live display — do NOT write to DB here
                liveVoltage.value = received
                val concentration = sharedViewModel.calculateConcentrationPublic(received)
                liveConcentration.value = concentration
            }
        }
        chartCoroutineScope.launch {
            try {
                sharedViewModel.allData.collect { data ->
                    allData.value = data
    //                    modelProducer1.tryRunTransaction {
    //                        lineSeries {
    //                            series(
    //                                y = allData.value.map { it.concentration },
    //                            )
    //                            series(
    //                                y = allData.value.map { it.voltage },
    //                            )
    //                        }
    //                    }
                        //println("\t Added 1")
                        modelProducer2.runTransaction {
                            lineSeries {
                                series(
                                    x = allData.value.sortedBy { it.concentration }
                                        .map { it.concentration },
                                    y = allData.value.sortedBy { it.concentration }.map { it.voltage },
                                )
                            }
                        }
                }
            } catch (e: Exception) {
                Log.e("PestiSafe", "Error collecting chart data: $e")
            }
        }
    }

    Column (modifier = Modifier.verticalScroll(rememberScrollState())){
        //MainChart(modelProducer1, modifier = Modifier.padding(10.dp), chartColors = listOf(Color.Black, Color.Cyan))
/*        CartesianChartHost(
//            rememberCartesianChart(
//                rememberLineCartesianLayer(),
//                startAxis = rememberStartAxis(),
//                bottomAxis = rememberBottomAxis()
//            ),
//            modelProducer = modelProducer,
        )*/
        MainChart(modelProducer2, modifier = Modifier.padding(15.dp), chartColors = listOf(Color.Black))

        Card(modifier = Modifier.padding(20.dp)){
            if (liveVoltage.value != null) {
                Text("Voltage: ${liveVoltage.value} V \nConcentration: ${liveConcentration.value} ppm")
            } else if (allData.value.isNotEmpty()) {
                Text("Voltage: ${allData.value.last().voltage} V \nConcentration: ${allData.value.last().concentration} ppm")
            }
        }
        Button(
            onClick = { sharedViewModel.updateConcentration() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
        ) {
            Text("Capture Reading")
        }

        val pesticides = sharedViewModel.pesticides
        var selectedPesticide by remember { mutableStateOf<Pesticide?>(null) }
        var selectedCommodity by remember { mutableStateOf<Commodity?>(null) }
        var showPesticideDropdown by remember { mutableStateOf(false) }
        var showCommodityDropdown by remember { mutableStateOf(false) }


            Box(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { showPesticideDropdown = true }) {
                    Text(selectedPesticide?.name ?: "Select Pesticide")
                }
                DropdownMenu(
                    expanded = showPesticideDropdown,
                    onDismissRequest = { showPesticideDropdown = false }
                ) {
                    pesticides.forEach { pesticide ->
                        DropdownMenuItem(onClick = {
                            selectedPesticide = pesticide
                            showPesticideDropdown = false
                        }, text = {
                            Text(pesticide.name)
                        })
                    }
                }
            }

            if (selectedPesticide != null) {
                var commodities : List<Commodity> by remember { mutableStateOf(emptyList()) }
                LaunchedEffect(selectedPesticide?.id) {
                    withContext(Dispatchers.IO){
                        commodities = sharedViewModel.repository.mrlDao.getMRLs(selectedPesticide!!.id)
                            .mapNotNull { sharedViewModel.repository.commodityDao.getCommodity(it.commodityID) }
                    }
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { showCommodityDropdown = true}) {
                        Text(selectedCommodity?.name ?: "Select Commodity")
                    }
                    DropdownMenu(
                        expanded = showCommodityDropdown,
                        onDismissRequest = { showCommodityDropdown = false }
                    ) {
                        commodities.forEach { commodity ->
                            DropdownMenuItem(onClick = {
                                selectedCommodity = commodity
                                showCommodityDropdown = false
                            }, text = {
                                Text(commodity.name)
                            })
                        }
                    }
                }
            }

            if (selectedCommodity != null) {
                var mrl by remember { mutableStateOf<MRL?>(null) }
                LaunchedEffect(selectedPesticide?.id, selectedCommodity?.id) {
                    withContext(Dispatchers.IO){
                        mrl = sharedViewModel.repository.mrlDao.getMRL(
                            selectedPesticide!!.id,
                            selectedCommodity!!.id
                        )
                    }
                }
                if (mrl != null) {
                    Card(modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)){
                        Column(modifier = Modifier.padding(10.dp).fillMaxSize().align(Alignment.CenterHorizontally)) {
                            Text("Maximum Residue Limit: ${mrl!!.mrl}")
                            val textValue = remember { mutableStateOf("") }
                            val textColor = remember { mutableStateOf(Color.Black)    }
                            Text(text=textValue.value, color=textColor.value, modifier = Modifier.padding(5.dp), fontSize = 25.sp, textAlign = TextAlign.Center)
                            if (allData.value.isNotEmpty()){
                                if (allData.value.last().concentration > mrl!!.mrl){
                                    textValue.value = "${allData.value.last().concentration} exceeds the residue limit. \nThis is not safe for consumption"
                                    textColor.value = Color.Red
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Icon(Icons.Filled.AssignmentLate, contentDescription = "Scan complete", tint = Color.Red, modifier = Modifier.size(30.dp).align(Alignment.CenterHorizontally))

                                }
                                else {
                                    textValue.value = "${allData.value.last().concentration} is within the residue limit. \nThis is safe for consumption"
                                    textColor.value = Color.Green
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Icon(Icons.Filled.Check, contentDescription = "Scan complete", tint = Color.Green, modifier = Modifier.size(30.dp).align(Alignment.CenterHorizontally))
                                }
                            }
                        }
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
                    in listOf("CSV","JSON") -> downloadFile(
                        context,
                        "${sharedViewModel.user?.username}_${selectedPesticide?.name}_${selectedCommodity?.name}_data.${chosenFormat.value.lowercase()}",
                        allData.value.toTypedArray(),
                        chosenFormat.value
                    )
                    "Excel" -> downloadFile(
                        context,
                        "${sharedViewModel.user?.username}_${selectedPesticide?.name}_${selectedCommodity?.name}_data.xlsx",
                        allData.value.toTypedArray(),
                        chosenFormat.value,
                        username = sharedViewModel.user?.username ?: "",
                        pesticide = selectedPesticide?.name ?: "",
                        commodity = selectedCommodity?.name ?: ""
                    )
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




