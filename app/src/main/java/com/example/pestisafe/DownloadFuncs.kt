package com.example.pestisafe

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        println("No ${fileType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }} Viewer Found. Error $e")
        Toast.makeText(context, "Could not find a ${fileType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(
            Locale.getDefault()) else it.toString() }} Viewer", Toast.LENGTH_SHORT).show()
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
    com.example.pestisafe.v1.openDownloadedFile(context, file.absolutePath)
}