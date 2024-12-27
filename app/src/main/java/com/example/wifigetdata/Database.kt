package com.example.wifigetdata

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * The data class for the data values
 * @property voltage the voltage value
 * @property concentration the concentration value
 * @property id the id of the data value
 */
@Entity(tableName = "DataValue")
data class DataValue (
    @ColumnInfo(name="Voltage") val voltage: Double,
    @ColumnInfo(name="Concentration") val concentration:Double,
    @PrimaryKey(autoGenerate = true) val id:Int = 0
) {
    fun str(): String {
        return "$voltage V, $concentration ppm"
    }
}
/**
 * The data class for the pesticides
 * @property id the id of the pesticide
 * @property name the name of the pesticide
 */
@Entity(tableName = "Pesticide")
data class Pesticide(
    @PrimaryKey
    val id: Int,
    val name: String
)

/**
 * The data class for the commodities
 * @property id the id of the commodity
 * @property name the name of the commodity
 */
@Entity(tableName = "Commodity")
data class Commodity(
    val name: String,
    @PrimaryKey(autoGenerate = true) val id :Int
)

/**
 * The data class for the maximum residue limits
 * @property pesticide the id of the pesticide
 * @property commodity the id of the commodity
 * @property mrl the maximum residue limit
 * @property mrlID the id of the mrl
 */
@Entity(
    tableName = "MRL",
    foreignKeys = [
        ForeignKey(entity = Pesticide::class, parentColumns = ["id"], childColumns = ["pesticide"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Commodity::class, parentColumns = ["id"], childColumns = ["commodity"], onDelete= ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["pesticide"]), Index(value = ["commodity"])],
    )
data class MRL(
    val pesticide: Int,
    val commodity: Int,
    val mrl: Double,
    @PrimaryKey(autoGenerate = true) val mrlID :Int = 0
)

/**
 * The data access object for the data values
 */
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

/**
 * The data access object for the pesticides
 */
@Dao
interface PesticideDao {
    @Query("SELECT * FROM Pesticide")
    fun getAll(): LiveData<List<Pesticide>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg items: Pesticide)

    @Query("SELECT * FROM Pesticide WHERE name = :name")
    fun getPesticide(name: String): Pesticide

    @Query("SELECT * FROM Pesticide WHERE id = :id")
    fun getPesticide(id: Int): Pesticide
}

/**
 * The data access object for the commodities
 */
@Dao
interface CommodityDao {
    @Query("SELECT * FROM Commodity")
    fun getAll(): List<Commodity>

    @Insert
    fun insert(vararg items: Commodity)

    @Query("SELECT * FROM Commodity WHERE name = :name")
    fun getCommodity(name: String): Commodity

    @Query("SELECT * FROM Commodity WHERE id = :id")
    fun getCommodity(id: Int): Commodity
}

/**
 * The data access object for the maximum residue limits
 */
@Dao
interface MRLDao {
    @Query("SELECT * FROM MRL")
    fun getAll(): LiveData<List<MRL>>

    @Insert
    fun insert(vararg items: MRL)

    @Query("SELECT * FROM MRL WHERE pesticide = :pesticideID")
    fun getMRLs(pesticideID: Int): List<MRL>

    @Query ("SELECT * FROM MRL WHERE pesticide = :pesticide AND commodity = :commodity")
    fun getMRL(pesticide: Int, commodity: Int): MRL
}

/**
 * The database class
 */
@Database(entities = [DataValue::class, Pesticide::class, Commodity::class, MRL::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dataValueDao(): DataValueDao
    abstract fun pesticideDao(): PesticideDao
    abstract fun commodityDao(): CommodityDao
    abstract fun mrlDao(): MRLDao

}

/**
 * The repository class
 * @property appDatabase the database
 */
class Repository(private val appDatabase: AppDatabase){
    val dataValueDao = appDatabase.dataValueDao()
    val pesticideDao = appDatabase.pesticideDao()
    val commodityDao = appDatabase.commodityDao()
    val mrlDao = appDatabase.mrlDao()

    suspend fun deleteAll() {
        dataValueDao.deleteAll()
    }
    /**
     * Inserts a data value into the database
     * @param dataValue the data value to insert
     */
    suspend fun insert(dataValue: DataValue) {
        dataValueDao.insert(dataValue)
    }
    /**
     * Gets a data value from a voltage
     * @param voltage the voltage to get the data value from
     * @return the data value
     */
    suspend fun getFromVoltage(voltage: Double): DataValue {
        return dataValueDao.getFromVoltage(voltage)
    }
    }