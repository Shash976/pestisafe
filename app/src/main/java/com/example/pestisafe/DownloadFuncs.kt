package com.example.pestisafe

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.gson.Gson
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
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

fun arrayToExcel(data: List<DataValue>, username: String, pesticide: String, commodity: String): ByteArray {
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("PestiSafe Data")

    // Header row
    val headerRow = sheet.createRow(0)
    headerRow.createCell(0).setCellValue("ID")
    headerRow.createCell(1).setCellValue("Voltage (V)")
    headerRow.createCell(2).setCellValue("Concentration (ppm)")
    headerRow.createCell(3).setCellValue("Username")
    headerRow.createCell(4).setCellValue("Pesticide")
    headerRow.createCell(5).setCellValue("Commodity")

    // Data rows
    data.forEachIndexed { index, value ->
        val row = sheet.createRow(index + 1)
        row.createCell(0).setCellValue(value.id.toDouble())
        row.createCell(1).setCellValue(value.voltage)
        row.createCell(2).setCellValue(value.concentration)
        row.createCell(3).setCellValue(username)
        row.createCell(4).setCellValue(pesticide)
        row.createCell(5).setCellValue(commodity)
    }

    // Auto-size columns
    (0..5).forEach { sheet.autoSizeColumn(it) }

    val outputStream = ByteArrayOutputStream()
    workbook.write(outputStream)
    workbook.close()
    return outputStream.toByteArray()
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
        Formats.Excel.toFormat().lowercase() -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
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
        Log.e("PestiSafe", "No ${fileType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }} Viewer Found. Error $e")
        Toast.makeText(context, "Could not find a ${fileType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(
            Locale.getDefault()) else it.toString() }} Viewer", Toast.LENGTH_SHORT).show()
    }
}

fun downloadFile(context: Context, fileName:String, array: Array<DataValue>, formatStr: String, username: String = "", pesticide: String = "", commodity: String = ""){
    val format = Formats.valueOf(formatStr)
    val dateFormat = SimpleDateFormat("ddMMMyy", Locale.getDefault())
    val date = dateFormat.format(Date())
    val nameWithoutExtension = fileName.substringBeforeLast(".")
    val extension = fileName.substringAfterLast(".")
    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    var file = File(dir, "$nameWithoutExtension-${date.uppercase()}.$extension")

    val baseNameWithoutExtension = file.nameWithoutExtension
    var counter = 1
    while (file.exists()) {
        file = File(dir, "$baseNameWithoutExtension($counter).$extension")
        counter++
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Android 10+ — WRITE_EXTERNAL_STORAGE is deprecated and ignored by the system;
        // apps can write to the public Downloads directory without any special permission.
        file.createNewFile()

        if (format == Formats.Excel) {
            val bytes = arrayToExcel(array.toList(), username, pesticide, commodity)
            file.writeBytes(bytes)
        } else {
            val content = convertFromArray(array, format)
            val outputStream = FileOutputStream(file)
            outputStream.write(content.toByteArray())
            outputStream.close()
        }
        Toast.makeText(context, "File downloaded successfully", Toast.LENGTH_SHORT).show()
        openDownloadedFile(context, file.absolutePath)
    } else {
        // Android 9 and below — check legacy WRITE_EXTERNAL_STORAGE permission.
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                context as ComponentActivity,
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                ),
                PackageManager.PERMISSION_GRANTED
            )
            // Return early — the user must grant permission before we can create or write the file.
            // The download should be re-initiated after permissions are granted.
            return
        }

        file.createNewFile()

        if (format == Formats.Excel) {
            val bytes = arrayToExcel(array.toList(), username, pesticide, commodity)
            file.writeBytes(bytes)
        } else {
            val content = convertFromArray(array, format)
            val outputStream = FileOutputStream(file)
            outputStream.write(content.toByteArray())
            outputStream.close()
        }
        Toast.makeText(context, "File downloaded successfully", Toast.LENGTH_SHORT).show()
        openDownloadedFile(context, file.absolutePath)
    }
}
