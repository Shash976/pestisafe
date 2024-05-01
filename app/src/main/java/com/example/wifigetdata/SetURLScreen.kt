package com.example.wifigetdata

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wifigetdata.ui.theme.WifigetdataTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SetURLScreen : MainActivity() {
    @SuppressLint("NotConstructor")
    @Composable
    fun SetURLScreen(){
        val (url, setURL) = remember {
            mutableStateOf("")
        }
        val coroutineScope = rememberCoroutineScope()
        val (receivedValue, setReceivedValue) = remember { mutableStateOf<String>("") }
        Column(
            modifier= Modifier.padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            OutlinedTextField(value = url,
                placeholder = { Text(text="Enter IP Address") }, label = { Text(text="API") },
                keyboardOptions =  KeyboardOptions(keyboardType = KeyboardType.Uri),
                onValueChange = {
                    setURL(it.trim())
                })
            Button(
                onClick = {
                    if (url.isNotEmpty()) {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                try {
                                    setReceivedValue(getRequest(url))
                                    BasicValues.setReceivedVal(receivedValue.toDouble())
                                    BasicValues.setURL(url)
                                }
                                catch (e : Exception){
                                    println("OOPS THIS IS THE ERROR $e")
                                }
                                finally {
                                    println("finals?")
                                }
                            }
                        }
                    }
                }, modifier = Modifier
                    .padding(5.dp)
            ) {
                Text(text = "Click to set URL", fontWeight = FontWeight.ExtraBold, fontSize=20.sp,color= Color.White)
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun SetURLScreenPreview(){
        WifigetdataTheme {
            SetURLScreen()
        }
    }
}
