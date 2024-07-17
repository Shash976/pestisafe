package com.example.wifigetdata

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.text.Format
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class DropdownTest : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent{
            val openDialog = remember { mutableStateOf(false) }
            val selected = remember { mutableStateOf("") }
            val chosenFormat = remember { mutableStateOf("")   }
            val X_array = Array(20){ Random.Default.nextDouble()}
            val Y_array = Array(20){Random.Default.nextDouble()}
            val data_vals = Array(20){ DataValue(X_array[it], Y_array[it])}
            val context = this
            val options = listOf("CSV", "JSON", "Excel")

            fun arrayToCSV(array:Array<DataValue>):String{
                val stringBuilder = StringBuilder()
                stringBuilder.appendLine("id, voltage, concentration")
                array.forEach {
                    stringBuilder.appendLine("${it.id}, ${it.voltage}, ${it.concentration}")
                }
                return stringBuilder.toString()
            }

            fun arrayToJSON(array:Array<DataValue>) :String {
                val json = Gson().toJson(array)
                return json
            }

            fun convertFromArray(array: Array<DataValue>, format:String) :String {
                return when (format) {
                    "CSV" -> arrayToCSV(array)
                    "JSON" -> arrayToJSON(array)
                    else -> ""
                }
            }

            fun openDownloadedFile(context: Context, filePath: String) {
                val file = File(filePath)
                val fileName = File(filePath).name
                val fileType = fileName.substringAfterLast(".")
                val type = when(fileType.lowercase()){
                    Formats.CSV.toFormat().lowercase() -> "text/csv"
                    Formats.JSON.toFormat().lowercase() -> "application/json"
                    else -> ""
                }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file) // Use Uri directly for public file
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, type)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
                }
                try {

                    context.startActivity(intent)
                } catch (e: Exception) {
                    println("No ${fileType.capitalize(Locale.ROOT)} Viewer Found. Error $e")
                    Toast.makeText(context, "Could not find a ${fileType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} Viewer", Toast.LENGTH_SHORT).show()
                }
            }

            fun downloadFile(context: Context, fileName:String, array: Array<DataValue>, format: String="CSV"){
                val content = convertFromArray(array, format)
                val dateFormat = SimpleDateFormat("ddMMMyy", Locale.getDefault())
                val date = dateFormat.format(Date())
                val nameWithoutExtension = fileName.substringBeforeLast(".")
                val extension = fileName.substringAfterLast(".")
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                var file = File(dir, "$nameWithoutExtension-${date.uppercase()}.$extension")

                var counter  =1
                while(file.exists()){
                    println("File exists. Making new file")
                    val newNameWithoutExtension = file.name.substringBeforeLast(".")
                    file = File(dir, "$newNameWithoutExtension($counter).$extension")
                    counter++
                }

                file.createNewFile()
                println("FILE CREATED!! Filename: ${file.name}")

                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, android.Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    println("Permissions not given 😱 requesting now")
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        ),
                        PackageManager.PERMISSION_GRANTED
                    )
                    println("Got permission. downloading file")
                } else {
                    println(" PERMISSIONS GO. DOWNLOADING FILE")
                }

                val outputStream = FileOutputStream(file)
                outputStream.write(content.toByteArray())
                outputStream.close()
                Toast.makeText(context, "File downloaded successfully", Toast.LENGTH_SHORT).show()
                openDownloadedFile(context, file.absolutePath)
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Button(onClick = { openDialog.value = true }) {
                    Text(text = "Choose Format")
                }
                if (openDialog.value){
                    Dialog(onDismissRequest = { openDialog.value = false}) {
                            Card (modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .height(950.dp)
                                .wrapContentSize(Alignment.Center), shape = RoundedCornerShape(20.dp)){
                                Text(text="Formats", modifier = Modifier
                                    .padding(10.dp)
                                    .padding(top = 2.dp, start = 4.dp),textAlign = TextAlign.Center, fontSize = 20.sp)

                                Column {
                                    options.forEach {
                                        Row(modifier = Modifier.padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(selected = (it==selected.value), onClick = { selected.value = it })
                                            Text(text = it, modifier = Modifier
                                                .padding(1.dp)
                                                .wrapContentSize(Alignment.Center), textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                                Row (horizontalArrangement = Arrangement.End, modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(3.dp)){
                                    TextButton(onClick = { openDialog.value = false;}, shape= RoundedCornerShape(4.dp)) {
                                        Text(text = "Cancel")
                                    }
                                    TextButton(enabled = (selected.value in options), onClick = { chosenFormat.value = selected.value ; openDialog.value=false; }) {
                                        Text(text = "Set Format")
                                    }
                                }
                            }
                    }
                }
                Text(text = "The selected option is ${chosenFormat.value}")
                Button(shape = RoundedCornerShape(5.dp), enabled = (chosenFormat.value in options), modifier=Modifier.padding(20.dp), onClick = {
                    when (chosenFormat.value) {
                         in listOf("CSV","JSON") -> downloadFile(context,"data.${chosenFormat.value.lowercase()}", data_vals, chosenFormat.value)
                        "Excel" -> Toast.makeText(context, "Not available yet", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(text = "Download ${chosenFormat.value}")
                }
            }
        }
    }

}


fun generateFile(context: Context, fileName :String) :File?{
    val file = File(context.filesDir, fileName)
    file.createNewFile()
    return if (file.exists()) {file} else {null}
}

fun goToFileIntent(context: Context, file:File) :Intent{
    val intent = Intent(Intent.ACTION_VIEW)
    val contentUri = FileProvider.getUriForFile(context,"${context.packageName}.fileprovider", file)
    val mimeType = context.contentResolver.getType(contentUri)
    intent.setDataAndType(contentUri, mimeType)
    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    return intent
}

fun arrayToCSV(array:Array<DataValue>):String{
    val stringBuilder = StringBuilder()
    stringBuilder.appendLine("id, voltage, concentration")
    array.forEach {
        stringBuilder.appendLine("${it.id}, ${it.voltage}, ${it.concentration}")
    }
    return stringBuilder.toString()
}

fun arrayToJSON(array:Array<DataValue>) :String {
    val json = Gson().toJson(array)
    return json
}

fun convertFromArray(array: Array<DataValue>, format:Formats) :String {
    return when (format) {
        Formats.CSV -> arrayToCSV(array)
        Formats.JSON -> arrayToJSON(array)
        else -> ""
    }
}

fun openDownloadedFile(context: Context, filePath: String) {
    val file = File(filePath)
    val fileName = File(filePath).name
    val fileType = fileName.substringAfterLast(".")
    val type = when(fileType.lowercase()){
        Formats.CSV.toFormat().lowercase() -> "text/csv"
        Formats.JSON.toFormat().lowercase() -> "application/json"
        else -> ""
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file) // Use Uri directly for public file
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, type)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
    }
    try {

        context.startActivity(intent)
    } catch (e: Exception) {
        println("No ${fileType.capitalize(Locale.ROOT)} Viewer Found. Error $e")
        Toast.makeText(context, "Could not find a ${fileType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} Viewer", Toast.LENGTH_SHORT).show()
    }
}

fun downloadFile(context: Context, fileName:String, array: Array<DataValue>, formatStr: String){
    val format = Formats.valueOf(formatStr)
    val content = convertFromArray(array, format)
    val dateFormat = SimpleDateFormat("ddMMMyy", Locale.getDefault())
    val date = dateFormat.format(Date())
    val nameWithoutExtension = fileName.substringBeforeLast(".")
    val extension = fileName.substringAfterLast(".")
    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    var file = File(dir, "$nameWithoutExtension-${date.uppercase()}.$extension")

    var counter  =1
    while(file.exists()){
        println("File exists. Making new file")
        val newNameWithoutExtension = file.name.substringBeforeLast(".")
        file = File(dir, "$newNameWithoutExtension($counter).$extension")
        counter++
    }

    file.createNewFile()
    println("FILE CREATED!! Filename: ${file.name}")

    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, android.Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
        println("Permissions not given 😱 requesting now")
        ActivityCompat.requestPermissions(
            context as ComponentActivity,
            arrayOf(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
            ),
            PackageManager.PERMISSION_GRANTED
        )
        println("Got permission. downloading file")
    } else {
        println(" PERMISSIONS GO. DOWNLOADING FILE")
    }

    val outputStream = FileOutputStream(file)
    outputStream.write(content.toByteArray())
    outputStream.close()
    Toast.makeText(context, "File downloaded successfully", Toast.LENGTH_SHORT).show()
    openDownloadedFile(context, file.absolutePath)
}