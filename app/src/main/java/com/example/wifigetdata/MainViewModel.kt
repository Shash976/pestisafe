package com.example.wifigetdata

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlin.math.round

class MainViewModel(val repository: Repository) :ViewModel() {

    val url = mutableStateOf("")
    val theValue = MutableStateFlow(0.0)
    val r2score = MutableStateFlow(0.0)
    val calibrationConcentration = doubleArrayOf(1.0, 10.0, 5.0, 7.5, 6.0, 2.5, 4.0, 1.25)
    val allData = MutableStateFlow(emptyList<DataValue>())
    val gradient = mutableDoubleStateOf(0.0)
    val intercept = mutableDoubleStateOf(0.0)
    private val updateTiming: MutableState<Long> = mutableLongStateOf(3000)
    var screen :Routes = Routes.MAIN
    private val viewModelJob = SupervisorJob()

    private fun insert(dataValue: DataValue)  {
        viewModelScope.launch {
            repository.insert(dataValue)
        }
    }

    suspend fun getFromVoltage(voltage: Double): DataValue = repository.getFromVoltage(voltage)

    private fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    fun resetValues() {
        gradient.doubleValue = 0.0
        intercept.doubleValue = 0.0
        r2score.value = 0.0

        //url.value = ""
        deleteAll()
//        println(repository.dataValueDao.getAll().asLiveData().value)
//        println(repository.dataValueDao.getConcentrationArray().asLiveData().value)
//        println(repository.dataValueDao.getAll().asLiveData().value)
    }

    fun updateData(newVoltage: Double, newConcentration: Double) = viewModelScope.launch{
        insert(DataValue(voltage = newVoltage, concentration = newConcentration))
    }

    fun updateGradientIntercept(data: List<DataValue>) = viewModelScope.launch {
        val voltageArray = data.map { it.voltage }
        val concentrationArray = data.map { it.concentration }
        val (slope, c) = linearRegression( concentrationArray, voltageArray)
        gradient.doubleValue = slope
        intercept.doubleValue = c
    }

    fun updateConcentration() {
        val voltage = theValue.value
        val concentration = calculateConcentration(voltage)
        updateData(voltage, concentration)
    }

    private fun calculateConcentration(voltage: Double): Double {
        // Y = MX + C -> X = Y-C / M
        var concentration =  (voltage - intercept.doubleValue) / gradient.doubleValue
        concentration = round(concentration*1000)/1000
        return concentration
    }


    fun updateR2Score(dataArray:List<DataValue>) = viewModelScope.launch {
        r2score.value= calculateRSquared(
            dataArray.map { it.voltage }.orEmpty().toDoubleArray(),
            dataArray.map { it.concentration }.orEmpty().toDoubleArray()
        )
        println(r2score.value)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun fetchData() {
        println("Function called")
        viewModelScope.launch(viewModelJob) {
            while (true) {
                withContext(Dispatchers.IO) {
                    theValue.value  = updateReceivedValue()
                    println("${ theValue.value} , ${url.value }")
                }
                delay(updateTiming.value)
            }
        }
    }

    private suspend fun updateReceivedValue() :Double{
        try {
            val received = getRequest(url.value).toDouble()
            return received
        } catch (e: Exception) {
            println("OOPS THIS IS THE ERROR $e")
            println("URL IS ${url.value}")
        }
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

