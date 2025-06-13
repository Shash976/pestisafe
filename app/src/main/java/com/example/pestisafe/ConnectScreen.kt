package com.example.pestisafe

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun MainScreen(sharedViewModel: MainViewModel, navController: NavController) {
    val context = LocalContext.current
    val connectionState = remember { mutableStateOf(isWifiConnected(context)) }
    val labelText = remember { mutableStateOf("Not connected") }
    val buttonText = remember { mutableStateOf("Connect") }
    val pesticides = sharedViewModel.repository.pesticideDao.getAll().observeAsState(initial = emptyList()) // Observe LiveData
    val isLoading = remember { mutableStateOf(false) }

    // Show toast when not connected to Wi-Fi
    LaunchedEffect(connectionState.value) {
        if (!connectionState.value) {
            Toast.makeText(context, "Please connect to a Wi-Fi network", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle navigation when data is available
    LaunchedEffect(pesticides.value) {
        println("Current Pesticides List: ${pesticides.value}")
        if (isLoading.value && pesticides.value.isNotEmpty()) {
            isLoading.value = false
            navController.navigate(Routes.PESTICIDE_SELECTION.toString())
        }
    }

    val onButtonClick: () -> Unit = if (sharedViewModel.url.value.isNotEmpty()) {
        {
            sharedViewModel.url.value = ""
            connectionState.value = false
        }
    } else {
        {
            sharedViewModel.getPesticideData()
            sharedViewModel.resetValues()
            isLoading.value = true // Start loading when fetching data
        }
    }

    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(if (sharedViewModel.url.value.isNotEmpty()) "Connected to: ${sharedViewModel.url.value}" else "Not connected")
        Button(onClick = onButtonClick, enabled = connectionState.value && !isLoading.value) {
            Text(if (sharedViewModel.url.value.isNotEmpty()) "Disconnect" else "Connect")
        }
    }
}



fun isWifiConnected(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
}