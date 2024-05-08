package com.example.wifigetdata

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.lifecycle.ViewModelProvider
import com.example.wifigetdata.ui.theme.WifigetdataTheme

class SetURLActivity : ComponentActivity() {
    private lateinit var sharedViewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            WifigetdataTheme {
                SetURLScreen()
            }
        }
        sharedViewModel = ViewModelProvider((applicationContext as AppViewModel))[MainViewModel::class.java]
    }

    @SuppressLint("NotConstructor")
    @Composable
    fun SetURLScreen(){
        val (url, setURL) = remember {
            mutableStateOf("")
        }
        val intent = Intent(this@SetURLActivity, CalibrationActivity::class.java)
        val coroutineScope = rememberCoroutineScope()
        val (receivedValue, setReceivedValue) = remember { mutableStateOf<String>("") }
        Column(
            modifier= Modifier.padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            OutlinedTextField(value = url,
                placeholder = { Text(text="Enter IP Address") }, label = { Text(text="URL / IP Address") },
                keyboardOptions =  KeyboardOptions(keyboardType = KeyboardType.Uri),
                onValueChange = {
                    setURL(it.trim())
                })
            Button(
                onClick = {
                    println("\t\t\t BUTTON CLICKED")
                    if (url.isNotEmpty()) {
                        BasicValues.setURL(url)
                        sharedViewModel.fetchData()
                        println("fetch data function called")
                        startActivity(intent)
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
