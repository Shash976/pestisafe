package com.example.wifigetdata

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.example.wifigetdata.ui.theme.WifigetdataTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import kotlin.math.sqrt


open class MainActivity : ComponentActivity() {
    val database by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database").build()
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val databaseDataValues by database.dataValueDao().getAll().observeAsState(emptyList())
            val (receivedVal, setReceivedVal)  = remember { mutableDoubleStateOf(0.0) }
            suspend fun updateReceivedValue() {
                try {
                    BasicValues.setReceivedVal(getRequest(BasicValues.getURL()).toDouble())
                    setReceivedVal(BasicValues.getReceivedVal())
                }
                catch (e : Exception){
                    println("OOPS THIS IS THE ERROR $e")
                }
                finally {
                    println("finals?")
                }


            }
            WifigetdataTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //Greeting("Android")
                    val urlScreen = SetURLScreen()
                    urlScreen.SetURLScreen()

                    val calibrationActivity = CalibrationActivity()
                    calibrationActivity.CalibrationScreen()

                    val coroutineScope = rememberCoroutineScope()
                    coroutineScope.launch{
                        withContext(Dispatchers.IO){
                            while (BasicValues.getURL().isNotEmpty()) {
                                updateReceivedValue()
                                delay(1000)
                            }
                        }
                    }

                }
            }
        }
    }


}

/*
//fun updateR2Score(){
//    val meanVoltage =
//        BasicValues.getVoltageDataArray().sum() / BasicValues.getVoltageDataArray().size
//    val meanConcentration =
//        BasicValues.getConcentrationDataArray().sum() / BasicValues.getConcentrationDataArray().size
//
//    val voltageVar =
//        calculateSTDEVP(BasicValues.getVoltageDataArray(), meanVoltage)
//    val concentrationVar =
//        calculateSTDEVP(BasicValues.getConcentrationDataArray(), meanConcentration)
//
//    val ssVoltage = voltageVar * BasicValues.getVoltageDataArray().size
//    val ssConcentration = concentrationVar * BasicValues.getConcentrationDataArray().size
//
//    var sumProdDev = 0.0F
//    var counter = 0
//    while (counter < BasicValues.getConcentrationDataArray().size) {
//        sumProdDev += (BasicValues.getVoltageDataArray()[counter] - meanVoltage) * (BasicValues.getConcentrationDataArray()[counter] - meanConcentration)
//        counter++
//    }
//    BasicValues.setR2Score((sumProdDev / sqrt(ssVoltage * ssConcentration)).pow(2.0F))
//}
//
//fun calculateSTDEVP(array: MutableList<Float>, mean:Float) :Float{
//    var sumXSubMx = 0.0F;
//    array.forEach {
//        sumXSubMx += ((it - mean).toDouble()).pow(2.0).toFloat()
//    }
//    return sqrt((sumXSubMx.toDouble()/array.size)).toFloat() * 2.0F
//}*/

fun updateConcentration() {
    val voltage = BasicValues.getReceivedVal()
    val concentration = calculateConcentration(voltage)
    BasicValues.updateData(voltage, concentration)
}

fun calculateConcentration(voltage: Double): Double {
    // Y = MX + C -> X = Y-C / M
    return (voltage - BasicValues.getIntercept()) / BasicValues.getGradient()
}

fun updateGradientIntercept() {
    if (BasicValues.getConcentrationDataArray().size > 2) {
        val x1 = BasicValues.getConcentrationDataArray()[-2]
        val x2 = BasicValues.getConcentrationDataArray()[-1]
        val y1 = BasicValues.getVoltageDataArray()[-2]
        val y2 = BasicValues.getVoltageDataArray()[-1]
        val (gradient, intercept) = calcSlopeIntercept(x1, y1, x2, y2)
        BasicValues.setGradient(gradient)
        BasicValues.setIntercept(intercept)
    }
}

fun calcSlopeIntercept(x1 :Double, y1 :Double, x2 :Double, y2 :Double): Pair<Double, Double> {
    val gradient = (y2-y1) / (x2-x1)
    val intercept = y1-(gradient*x1)
    return gradient to intercept
}

fun updateR2Score() {
    val r2score = calculateRSquared(BasicValues.getVoltageDataArray().toDoubleArray(), BasicValues.getConcentrationDataArray().toDoubleArray())
    BasicValues.setR2Score(r2score)
}

fun calculateRSquared(array1: DoubleArray, array2: DoubleArray): Double {
    if (array1.size != array2.size) {
        throw IllegalArgumentException("Arrays must have the same size")
    }

    val n = array1.size
    val mean1 = array1.average()
    val mean2 = array2.average()

    var numerator = 0.0
    var denominator1 = 0.0
    var denominator2 = 0.0

    for (i in 0 until n) {
        val diff1 = array1[i] - mean1
        val diff2 = array2[i] - mean2
        numerator += diff1 * diff2
        denominator1 += diff1 * diff1
        denominator2 += diff2 * diff2
    }

    val r = numerator / sqrt(denominator1 * denominator2)
    return r * r
}


suspend fun getRequest(apiUrl: String) : String {
    val url: URL = URI.create(apiUrl).toURL()
    var response: StringBuilder? = null
    println("the URL part works IG")
        try {
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            println("The connection definition works fine")
            connection.requestMethod = "GET"
            val responseCode = connection.responseCode
            println("$responseCode is responses code")
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                var line: String?
                response = StringBuilder()

                while (reader.readLine().also { line = it } != null) {
                    response!!.append(line)
                }
                reader.close()

                connection.disconnect()

            }
        } catch (err: Exception ){
            println("$err \n\t...is the error")
        }
    return response?.toString() ?: ""
}

