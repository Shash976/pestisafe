package com.example.wifigetdata

import android.provider.ContactsContract.Data
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.math.sqrt
import androidx.navigation.NavHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import kotlin.math.sqrt

class MainViewModel(private val repository: Repository) :ViewModel(){
    val url = MutableStateFlow("")
    val theValue = MutableStateFlow(0.0)
    val r2score = mutableDoubleStateOf(0.0)
    val calibrationConcentration = doubleArrayOf( 1.0, 10.0, 5.0, 7.5, 6.0, 2.5, 4.0, 1.25)
    val allData =repository.allData

    val voltageDataArray = repository.voltageArray
    val concentrationDataArray = repository.concentrationArray


    val gradient  = mutableDoubleStateOf(0.0)
    val intercept = mutableDoubleStateOf(0.0)
    val updateTiming : MutableState<Long> = mutableLongStateOf(3000)
    private val viewModelJob = SupervisorJob()

    fun changeURL(newURL :String){
        url.value = newURL
    }

    fun insert(dataValue: DataValue) = viewModelScope.launch {
        repository.insert(dataValue)
    }

    suspend fun getFromVoltage(voltage:Double) :DataValue = repository.getFromVoltage(voltage)

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    fun updateData(newVoltage :Double, newConcentration :Double) {
        insert(DataValue(voltage = newVoltage, concentration = newConcentration))

        if (voltageDataArray.size > 2) {
            updateGradientIntercept()
        }
    }

    fun updateConcentration() {
        val voltage = theValue.value
        val concentration = calculateConcentration(voltage)
        updateData(voltage, concentration)
    }

    fun calculateConcentration(voltage: Double): Double {
        // Y = MX + C -> X = Y-C / M
        return (voltage - intercept.doubleValue) / gradient.doubleValue
    }

    fun updateGradientIntercept() {
        val size  = concentrationDataArray.size
        if (size > 2) {
            val (m,c) = linearRegression(concentrationDataArray, voltageDataArray)
            gradient.doubleValue = m
            intercept.doubleValue = c
        }
    }

    fun updateR2Score() = viewModelScope.launch{
        r2score.doubleValue = calculateRSquared(
            voltageDataArray.toDoubleArray(),
            concentrationDataArray.toDoubleArray()
        )
        println(r2score.doubleValue)
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
    fun fetchData() {
        println("Function called")
        viewModelScope.launch(viewModelJob){
            while (true) {
                url.value = BasicValues.getURL()
                withContext(Dispatchers.IO){
                    theValue.value = updateReceivedValue()
                    println(theValue.value)
                }
                delay(BasicValues.getUpdateTiming())
            }
        }
    }
    private suspend fun updateReceivedValue() :Double{
        try {
            val received = getRequest(BasicValues.getURL()).toDouble()
            BasicValues.setReceivedVal(received)
            return received
        } catch (e: Exception) {  println("OOPS THIS IS THE ERROR $e")  }
        return 0.0
    }
    private suspend fun getRequest(apiUrl: String): String {
        val url: URL = URI.create(apiUrl).toURL()
        var response: StringBuilder? = null
        try {
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            val responseCode = connection.responseCode
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
        } catch (err: Exception) {
            println("$err \n\t...is the error")
        }
        return response?.toString() ?: ""
    }


}

