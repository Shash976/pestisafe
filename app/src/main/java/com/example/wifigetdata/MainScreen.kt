package com.example.wifigetdata

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun MainScreen(sharedViewModel: MainViewModel, navController: NavController){
    val connectionState = remember { mutableStateOf(false)  }
    val labelText = remember{ mutableStateOf("") }
    val buttonText = remember{ mutableStateOf("") }
    var onButtonClick = {}
    when(sharedViewModel.url.value.isNotEmpty()){
        true -> {
            connectionState.value = true
            labelText.value = "Connected to: ${sharedViewModel.url.value}"
            buttonText.value = "Disconnect"
            onButtonClick = {
                sharedViewModel.url.value = ""
                connectionState.value = false
            }
        }
        false -> {
            connectionState.value = false
            labelText.value = "Not connected"
            buttonText.value = "Connect"
            onButtonClick = {
                navController.navigate(Routes.IP_SCANNER.toString())
                sharedViewModel.resetValues()
            }
        }
    }
    Column (modifier = Modifier.padding(10.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
        Text(labelText.value)
        Button(onClick = onButtonClick) {
            Text(buttonText.value)
        }
    }
}