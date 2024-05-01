package com.example.wifigetdata

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wifigetdata.ui.theme.WifigetdataTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CalibrationActivity : MainActivity() {
    private var counter = 0
    @Composable
    fun CalibrationScreen(){
        Column {
            Text(text = "R-Square score: ${BasicValues.getR2Score()}", modifier=Modifier.padding(10.dp))
            LazyVerticalGrid(columns = GridCells.FixedSize(165.dp), modifier = Modifier.padding(1.dp)) {
                repeat(3) {
                    item {
                        CalibrateCard(
                            index = it
                        )
                    }
                }
                while (counter>2 && counter<BasicValues.getCalibrationConcentration().size && BasicValues.getR2Score() < 0.9) {
                        item {
                            CalibrateCard(index = counter+1)
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

    @SuppressLint("CoroutineCreationDuringComposition")
    @Composable
    fun CalibrateCard(index :Int){
        val (labelText, setLabelText) = remember { mutableDoubleStateOf(0.0) }
        val concentration = BasicValues.getCalibrationConcentration()[index]
        var textFixed by remember{ mutableStateOf( counter !=index)}
        Card (modifier = Modifier.padding(10.dp), shape=CardDefaults.elevatedShape, elevation = CardDefaults.cardElevation(defaultElevation = 6.dp )){
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(10.dp)){
                val coroutineScope = rememberCoroutineScope()
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
                Button(shape= RoundedCornerShape(10.dp), enabled = !textFixed, onClick = {
                    textFixed = true
                    val newDataValue = DataValue(0, labelText, concentration)
                    database.dataValueDao().insert(newDataValue)
                    BasicValues.updateData(newDataValue.voltage, newDataValue.concentration)
                    counter++
                    if (BasicValues.getConcentrationDataArray().size >= 2) {
                        updateR2Score()
                        updateGradientIntercept()
                    }
                }) {
                    Text(text = "Set Voltage")
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun CalibrateCardPreview(){
        WifigetdataTheme {
            CalibrateCard(0)
        }
    }
}

