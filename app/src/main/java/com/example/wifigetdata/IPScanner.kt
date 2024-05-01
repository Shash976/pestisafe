package com.example.wifigetdata

import android.os.AsyncTask
import androidx.compose.runtime.Composable
import java.net.InetAddress
import java.util.ArrayList

suspend fun Scan(subnet:String = "192.168.1.", lower:Int = 100, upper:Int = 110, timeout:Int = 5000){
    for (i in lower..upper) {
        val host = "$subnet$i"
        try {
            val inetAddress = InetAddress.getByName(host)
            if (inetAddress.isReachable(timeout)){
                println(inetAddress.toString())
            }
        } catch (e:Exception){
            println("THIS IS THE ERROR $e")
        }

    }
}