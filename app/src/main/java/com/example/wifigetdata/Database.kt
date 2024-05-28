package com.example.wifigetdata

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "DataValue")
data class DataValue (
    @ColumnInfo(name="Voltage") val voltage: Double,
    @ColumnInfo(name="Concentration") val concentration:Double,
    @PrimaryKey(autoGenerate = true) val id:Int = 0
)

@Dao
interface DataValueDao {
    @Query("SELECT * FROM DataValue")
    fun getAll(): LiveData<List<DataValue>>

    @Query("SELECT Voltage FROM DataValue")
    fun getVoltageArray(): List<Double>

    @Query("SELECT Concentration FROM DataValue")
    fun getConcentrationArray(): List<Double>
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