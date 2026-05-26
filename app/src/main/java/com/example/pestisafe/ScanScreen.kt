package com.example.pestisafe

import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.InetAddress

@Composable
fun ScanScreen(sharedViewModel: MainViewModel, applicationContext: MainActivity, navController: NavController){
    val ipAddresses = remember { mutableStateOf(listOf<String>()) }
    val isScanning = remember { mutableStateOf(false) }
    val scanComplete = remember { mutableStateOf(false) }

    // LaunchedEffect keyed on isScanning.value: runs whenever isScanning becomes true,
    // and is automatically cancelled when the composable leaves composition or the key changes.
    LaunchedEffect(isScanning.value) {
        if (isScanning.value) {
            withContext(Dispatchers.IO) {
                val timeout = 1000
                sharedViewModel.resetURL()
                // Get Wifi service
                val wifiManager =
                    applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                // Get the IP address of the device and format it as a string
                val ipAddress = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
                // Extract the subnet from the IP address
                val subnet = ipAddress.substringBeforeLast(".")
                ipAddresses.value = listOf()
                /**
                 * Check the hosts on the network
                 */
                coroutineScope {
                    val activeHosts = List(254) { i ->
                        async(Dispatchers.IO) {
                            val host = "$subnet.$i"
                            val inet = InetAddress.getByName(host)
                            if (inet.isReachable(timeout)) "$host (${inet.canonicalHostName})" else null
                        }
                    }.awaitAll().filterNotNull()
                    ipAddresses.value = activeHosts
                }
            }
            isScanning.value = false
            scanComplete.value = true
            delay(5000)
            scanComplete.value = false
        }
    }

    //val intent = Intent(this@IPScanner, CalibrationActivity::class.java)
    Column (modifier = Modifier.fillMaxSize().padding(20.dp)){
        Text(text = "Scan for Devices")
        Row (horizontalArrangement = Arrangement.Center){
            Button(enabled = (!isScanning.value), onClick = { isScanning.value = true }, modifier= Modifier.padding(10.dp)) {
                Text(text = "Scan")
            }
            if (isScanning.value) {
                CircularProgressIndicator()
            } else if (scanComplete.value) {
                Icon(Icons.Filled.Check, contentDescription = "Scan complete", tint = Color.Green)
            }
        }
        LazyColumn(userScrollEnabled = true) {
            items(ipAddresses.value.size) { index ->
                Card(modifier = Modifier.padding(10.dp), onClick = {
                    isScanning.value = false
                    val ipRegex = Regex("""\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}""")
                    val ip = ipRegex.find(ipAddresses.value[index])?.value
                    if (ip == null) {
                        Log.e("PestiSafe", "ERROR: Could not parse IP address from '${ipAddresses.value[index]}'")
                        Toast.makeText(applicationContext, "Invalid IP address, please rescan", Toast.LENGTH_SHORT).show()
                        return@Card
                    }
                    sharedViewModel.url.value = "http://$ip"
                    sharedViewModel.getMRLData()
                    sharedViewModel.fetchData()
                    sharedViewModel.resetValues()
                    navController.navigate(Routes.CALIBRATION.toString())
                }){
                    Text(
                        text = ipAddresses.value[index],
                    )
                }
            }
        }
    }
}