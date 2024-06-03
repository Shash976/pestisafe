package com.example.wifigetdata

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

@Entity(tableName = "DataValue")
data class DataValue (
    @ColumnInfo(name="Voltage") val voltage: Double,
    @ColumnInfo(name="Concentration") val concentration:Double,
    @PrimaryKey(autoGenerate = true) val id:Int = 0
)

@Dao
interface DataValueDao {
    @Query("SELECT * FROM DataValue")
    fun getAll(): Flow<List<DataValue>>

    @Query("SELECT Voltage FROM DataValue")
    fun getVoltageArray(): Flow<List<Double>>

    @Query("SELECT Concentration FROM DataValue")
    fun getConcentrationArray(): Flow<List<Double>>
    @Insert
    suspend fun insert(vararg items: DataValue)

    @Query("SELECT (SELECT COUNT(*) FROM DataValue) == 0")
    suspend fun isEmpty() :Boolean

    @Query("DELETE FROM DataValue")
    suspend fun deleteAll()

    @Query("SELECT * FROM DataValue WHERE Voltage = :voltage")
    fun getFromVoltage(voltage: Double) :DataValue

    @Query("SELECT * FROM DataValue WHERE id = :id")
    fun getFromId(id :Int) :DataValue

}

@Database(entities = [DataValue::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dataValueDao(): DataValueDao

}

class Repository(val dataValueDao: DataValueDao){

    //val allData : Flow<List<DataValue>> = dataValueDao.getAll()
    //var voltageArray : Flow<List<Double>> = dataValueDao.getAll().map { it.map { dataValue -> dataValue.voltage } }
    //var concentrationArray :Flow<List<Double>>  = dataValueDao.getAll().map { it.map { dataValue -> dataValue.concentration } }

    suspend fun deleteAll() {
        dataValueDao.deleteAll()
    }

    suspend fun insert(dataValue: DataValue) {
        dataValueDao.insert(dataValue)
        //voltageArray = dataValueDao.getAll().map { it.map { dataValue -> dataValue.voltage } }
        //concentrationArray = dataValueDao.getAll().map { it.map { dataValue -> dataValue.concentration } }

    }

    suspend fun getFromVoltage(voltage: Double): DataValue {
        return dataValueDao.getFromVoltage(voltage)
    }
    }