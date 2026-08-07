package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "incident_reports")
data class IncidentReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val frontIdScanBase64: String?,
    val backIdScanBase64: String?,
    val insuranceScanBase64: String?,
    val secondIdScanBase64: String?,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface IncidentDao {
    @Insert
    suspend fun insertReport(report: IncidentReport)
    
    @Query("SELECT * FROM incident_reports")
    suspend fun getAllReports(): List<IncidentReport>
}

@Database(entities = [IncidentReport::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun incidentDao(): IncidentDao
}
