package com.example.wifigetdata

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.collection.mutableFloatFloatMapOf
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
    fun CalibrationScreen(){
        val counter  = remember { mutableIntStateOf(0) }

        @Composable
        fun CalibrateCard(index :Int){
            val (labelText, setLabelText) = remember { mutableDoubleStateOf(0.0) }
            val concentration = BasicValues.getCalibrationConcentration()[index]
            val textFixed by remember(counter){ derivedStateOf { counter.intValue !=index }}

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
                        println("Counter ${counter.intValue}")
                        val newDataValue = DataValue(0, labelText, concentration)
                        //database.dataValueDao().insert(newDataValue)
                        BasicValues.updateData(newDataValue.voltage, newDataValue.concentration)
                        counter.intValue += 1
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

        Column (modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center ){
            Text(text = "R-Square score: ${BasicValues.getR2Score()}", modifier=Modifier.padding(10.dp))
            LazyVerticalGrid(columns = GridCells.FixedSize(165.dp), modifier = Modifier.padding(1.dp), verticalArrangement = Arrangement.Center, horizontalArrangement = Arrangement.Center) {
                repeat(3) {
                    item {
                        CalibrateCard(index = it)
                    }
                }
                while (counter.intValue>2 && counter.intValue<BasicValues.getCalibrationConcentration().size && BasicValues.getR2Score() < 0.9) {
                    item {
                        CalibrateCard(index = counter.intValue+1)
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

