package com.example.wifigetdata

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.wifigetdata.ui.theme.WifigetdataTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CalibrationActivity : ComponentActivity() {

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

        @Composable
        fun CalibrateCard(index: Int) {
            val (labelText, setLabelText) = remember { mutableDoubleStateOf(0.0) }
            val concentration = BasicValues.getCalibrationConcentration()[index]
            val textFixed by remember(counter) { derivedStateOf { counter.intValue != index } }

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
                    val coroutineScope = rememberCoroutineScope()
                    val mathCoroutineScope = rememberCoroutineScope()
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
                        //val newDataValue = DataValue(0, labelText, concentration)
                        //database.dataValueDao().insert(newDataValue)
                        BasicValues.updateData(labelText, concentration)
                        println("$labelText, $concentration")
                        if (BasicValues.getConcentrationDataArray().size >= 2) {
                            mathCoroutineScope.launch {
                                withContext(Dispatchers.Default) {
                                    updateR2Score()
                                    println(BasicValues.getR2Score())
                                    updateGradientIntercept()
                                }
                            }
                            if (BasicValues.getR2Score()>=0.9){
                                val intent =
                                    Intent(this@CalibrationActivity, HomeActivity::class.java)
                                startActivity(intent)
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

        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            val r2score = remember { mutableDoubleStateOf(0.0) }
            val r2CoroutineScope = rememberCoroutineScope()
            val additionalCards = remember { mutableIntStateOf(0) }
            val shownAdditionalCards = remember { mutableIntStateOf(0) }

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



            r2CoroutineScope.launch {
                withContext(Dispatchers.Default) {
                    while (true) {
                        r2score.doubleValue = BasicValues.getR2Score()
                        println("\t\t THIS IS r2 ${r2score.doubleValue}")
                        val isR2less =
                            counter.intValue > 2 && counter.intValue < BasicValues.getCalibrationConcentration().size && r2score.doubleValue < 0.9
                        println("\t is r2 less? $isR2less")
                        if (isR2less && shownAdditionalCards.intValue < BasicValues.getCalibrationConcentration().size - 3) {
                            shownAdditionalCards.intValue++ // Show one additional card
                        }
                        delay(10000) // Adjust delay between showing cards
                    }
                }
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

