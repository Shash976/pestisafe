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

@Entity
data class DataValue (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name="Voltage") val voltage: Double,
    @ColumnInfo(name="Concentration") val concentration:Double
)

@Dao
interface DataValueDao {
    @Query("SELECT * FROM DataValue")
    fun getAll(): LiveData<List<DataValue>>

    @Insert
    fun insert(vararg items: DataValue)

    @Query("SELECT (SELECT COUNT(*) FROM DataValue) == 0")
    fun isEmpty() :Boolean

}

@Database(entities = [DataValue::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dataValueDao(): DataValueDao

}