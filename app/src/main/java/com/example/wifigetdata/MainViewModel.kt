package com.example.wifigetdata

import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

class MainViewModel :ViewModel(){
    private val url = MutableStateFlow("")
    private val value = MutableStateFlow(0.0)
    private val viewModelJob = SupervisorJob()

    fun onURLChange(newURL :String){
        url.value = newURL
    }

    fun fetchData() {
        println("Function called")
        viewModelScope.launch(viewModelJob){
            while (true) {
                url.value = BasicValues.getURL()
                withContext(Dispatchers.IO){
                    value.value = updateReceivedValue()
                    println(value.value)
                }
                delay(1500)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
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
        } catch (err: Exception) {
            println("$err \n\t...is the error")
        }
        return response?.toString() ?: ""
    }
}

