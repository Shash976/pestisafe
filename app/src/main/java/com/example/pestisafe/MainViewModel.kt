package com.example.pestisafe

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import kotlin.math.round

/**
 * The ViewModel class for the main screen
 * @param repository the repository to use
 * @property url the url to get the data from
 * @see Repository
 * @see DataValue
 * @see Pesticide
 */
class MainViewModel(val repository: Repository) :ViewModel() {

    var url = mutableStateOf("")
    val theValue = MutableStateFlow(0.0)
    val r2score = MutableStateFlow(0.0)
    val calibrationConcentration = doubleArrayOf(1.0, 10.0, 5.0, 7.5, 6.0, 2.5, 4.0, 1.25)
    val allData = MutableStateFlow(emptyList<DataValue>())
    val gradient = mutableDoubleStateOf(0.0)
    val intercept = mutableDoubleStateOf(0.0)
    private val updateTiming: MutableState<Long> = mutableLongStateOf(3000)
    var screen :Routes = Routes.MAIN
    private val viewModelJob = SupervisorJob()
    var pesticides = emptyList<Pesticide>()
    var user :User? = null

    fun deleteAllPesticides(){
        viewModelScope.launch {
            repository.pesticideDao.deleteAll()
        }
    }

    private fun insert(dataValue: DataValue)  {
        viewModelScope.launch {
            repository.insert(dataValue)
        }
    }

    suspend fun getFromVoltage(voltage: Double): DataValue? = repository.getFromVoltage(voltage)

    private fun deleteAll() = viewModelScope.launch {
        repository.dataValueDao.deleteAll()
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

    fun resetURL() {
        url.value = ""
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

    fun calculateConcentrationPublic(voltage: Double): Double = calculateConcentration(voltage)

    private fun calculateConcentration(voltage: Double): Double {
        // Y = MX + C -> X = Y-C / M
        if (gradient.doubleValue == 0.0) return 0.0
        var concentration =  (voltage - intercept.doubleValue) / gradient.doubleValue
        if (concentration.isNaN() || concentration.isInfinite()) return 0.0
        concentration = round(concentration*1000)/1000
        return concentration
    }

    /**
     * Function to update the R2 score
     */
    fun updateR2Score(dataArray:List<DataValue>) = viewModelScope.launch {
        r2score.value= calculateRSquared(
            dataArray.map { it.voltage }.orEmpty().toDoubleArray(),
            dataArray.map { it.concentration }.orEmpty().toDoubleArray()
        )
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    /**
     * Function to start data fetching
     */
    fun fetchData() {
        viewModelScope.launch(viewModelJob) {
            while (url.value.isNotEmpty()) {
                ensureActive()
                withContext(Dispatchers.IO) {
                    theValue.value  = updateReceivedValue()
                }
                delay(updateTiming.value)
            }
        }
    }

    /**
     * Function to update the received value
     * @return the received value
     */
    private suspend fun updateReceivedValue() :Double{
        try {
            val received = getRequest(url.value).toDoubleOrNull() ?: return 0.0
            return received
        } catch (e: Exception) {
            Log.e("PestiSafe", "Error fetching value: $e")
            Log.e("PestiSafe", "URL: ${url.value}")
        }
        return 0.0
    }

    /**
     * Function to get the pesticide data
     * @see Pesticide
     */
    fun getPesticideData(){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                val apiUrl = "https://www.fao.org/jsoncodexpest/jsonrequest/pesticides/index.html"
                val response = getRequest(apiUrl)
                val gson = GsonBuilder().registerTypeAdapter(
                    PesticidesResponse::class.java,
                    PesticideDeserializer()
                ).create()
                val pesticideL = gson.fromJson(response, PesticidesResponse::class.java)
                pesticides = pesticideL.pesticides
                pesticides.forEach { repository.pesticideDao.insert(it) }
            }
        }
    }

    /**
     * Function to get the pesticide data
     * @see Pesticide
     * @see Detail
     * @see MRL
     * @see Commodity
     */
    fun getMRLData() {
        viewModelScope.launch(viewModelJob) {
            withContext(Dispatchers.IO) {
                pesticides.forEach {
                        val pesticideApi =
                            "https://www.fao.org/jsoncodexpest/jsonrequest/pesticides/details.html?id=${it.id}&lang=en"
                        val pesticideResponse = getRequest(pesticideApi)
                        val detailGson =
                            GsonBuilder().registerTypeAdapter(Detail::class.java, DetailDeserializer())
                                .create()
                        try {
                            val detail: Detail = detailGson.fromJson(pesticideResponse, Detail::class.java)
                            if (it.id !in repository.pesticideDao.getAllDirect().map { it.id }) {
                                repository.pesticideDao.insert(it)
                            }

                            detail.mrls.forEach { mrlDetail ->
                                if (mrlDetail.commodity.id !in repository.commodityDao.getAll()
                                        .map { it.id }
                                ) {
                                    repository.commodityDao.insert(mrlDetail.commodity)
                                }
                                val mrl = MRL(
                                    mrlDetail.pesticide.id,
                                    mrlDetail.commodity.id,
                                    mrlDetail.mrl
                                )
                                repository.mrlDao.insert(mrl)
                            }
                        } catch (e: Exception) {
                            Log.e("PestiSafe", "Failed to parse MRL data for pesticide: ${e.message}")
                        }
                }
            }

        }
    }

    /**
     * Function to get the request from the API
     * @param apiUrl the url to get the request from
     * @return the response from the API
     */
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
                    response.append(line)
                }
                reader.close()

                connection.disconnect()

            }
        } catch (err: Exception) {
            Log.e("PestiSafe", "$err \n\t...is the error")
        }
        return response?.toString() ?: ""
    }


}

