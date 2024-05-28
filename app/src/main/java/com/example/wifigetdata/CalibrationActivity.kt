package com.example.wifigetdata

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wifigetdata.ui.theme.WifigetdataTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CalibrationActivity : MainActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            WifigetdataTheme {
                CalibrationScreen()
            }
        }
    }


    @SuppressLint("CoroutineCreationDuringComposition")
    @Composable
    fun CalibrationScreen() {
        val counter = remember { mutableIntStateOf(0) }
        val r2score = remember { mutableDoubleStateOf(0.0) }
        val r2CoroutineScope = rememberCoroutineScope()
        val additionalCards = remember { mutableIntStateOf(0) }
        val shownAdditionalCards = remember { mutableIntStateOf(0) }
        val startR2Math = remember{ mutableIntStateOf(0) }


        @Composable
        fun CalibrateCard(index: Int) {
            val (labelText, setLabelText) = remember { mutableDoubleStateOf(0.0) }
            val concentration = BasicValues.getCalibrationConcentration()[index]
            val textFixed by remember(counter) { derivedStateOf { counter.intValue != index } }
            val coroutineScope = rememberCoroutineScope()
            val mathCoroutineScope = rememberCoroutineScope()

            Card(
                modifier = Modifier.padding(10.dp),
                shape = CardDefaults.elevatedShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(10.dp)
                ) {
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            while (BasicValues.getURL().isNotEmpty() && !textFixed) {
                                if (BasicValues.getReceivedVal() != labelText) {
                                    setLabelText(BasicValues.getReceivedVal())
                                }
                            }
                        }
                    }
                    Text(text = "$labelText V")
                    Text(text = "$concentration μm")
                    Button(shape = RoundedCornerShape(10.dp), enabled = !textFixed, onClick = {
                        println("Counter ${counter.intValue}")
                        val newDataValue = DataValue(labelText, concentration)
                        BasicValues.updateData(labelText, concentration)
                        println("$labelText, $concentration")
                        val arraySize = sharedViewModel.concentrationDataArray.size
                        if (arraySize >= 2) {
                            mathCoroutineScope.launch {
                                withContext(Dispatchers.Default) {
                                    sharedViewModel.insert(newDataValue)
                                    sharedViewModel.updateR2Score()
                                    println("This is the r2score ${BasicValues.getR2Score()}")
                                    sharedViewModel.updateGradientIntercept()
                                    startR2Math.intValue++
                                    if (arraySize>2){
                                        if (arraySize==counter.intValue+1 || sharedViewModel.r2score.doubleValue>=0.9){
                                            if (BasicValues.getCalibrationConcentration().size==counter.intValue+1) {
                                                println("All calibration values calibrated. Switching to main screen")
                                            } else if ( sharedViewModel.r2score.doubleValue>=0.9){
                                                println("R2score is sufficient. Switching to main screen")
                                            }
                                            val intent = Intent(
                                                    this@CalibrationActivity,
                                                    HomeActivity::class.java
                                                )
                                            startActivity(intent)
                                        }
                                    }
                                }
                            }

                        }
                        println("${BasicValues.getConcentrationDataArray()} : Concentration")
                        println("${BasicValues.getVoltageDataArray()} : Voltage")
                        counter.intValue = index + 1
                    }) {
                        Text(text = "Set Voltage")
                    }
                }
            }
        }

        LaunchedEffect((startR2Math.intValue)) {
            r2CoroutineScope.launch {
                withContext(Dispatchers.Default) {
                        r2score.doubleValue = sharedViewModel.r2score.doubleValue
                        val isR2less = counter.intValue > 2 && counter.intValue < sharedViewModel.concentrationDataArray.size && r2score.doubleValue < 0.9
                        println("\t is r2 (${r2score.doubleValue} less? $isR2less | Gradient: ${sharedViewModel.gradient.doubleValue}, Intercept: ${sharedViewModel.intercept.doubleValue} ")
                        if (isR2less && shownAdditionalCards.intValue < BasicValues.getCalibrationConcentration().size - 3) {
                            shownAdditionalCards.intValue++ // Show one additional card
                        }
                        delay(sharedViewModel.updateTiming.value + 1000) // Adjust delay between showing cards
                }
            }
        }

        Box(modifier=Modifier.fillMaxSize()){
            Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "R-Square score: ${r2score.doubleValue}",
                    modifier = Modifier.padding(10.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.FixedSize(165.dp),
                    modifier = Modifier.padding(1.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalArrangement = Arrangement.Center
                ) {

                    repeat(BasicValues.getCalibrationConcentration().size) {
                        val isVisible = it < 3 + shownAdditionalCards.intValue
                        if (isVisible) {
                            println("Showing Card number ${it + 1}")
                            item {
                                CalibrateCard(index = it)
                            }
                        }
                    }
                }
            }
            val context = LocalContext.current
            val databaseCoroutineScope = rememberCoroutineScope()
            FloatingActionButton(onClick = {
                BasicValues.resetValues()
                databaseCoroutineScope.launch {
                    withContext(Dispatchers.IO){
                        sharedViewModel.deleteAll()
                    }
                }
                (context as ComponentActivity).recreate()
            },modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)) {
                Icon(Icons.Filled.Refresh, contentDescription = "Reset")

            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun CalibrationScreenPreview(){
        CalibrationScreen()
    }

    @Preview(showBackground = true)
    @Composable
    fun CalibrateCardPreview(){
        WifigetdataTheme {
            //CalibrateCard(0)
        }
    }
}

