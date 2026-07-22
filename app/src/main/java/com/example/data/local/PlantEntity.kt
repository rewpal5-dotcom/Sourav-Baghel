package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nursery_plants")
data class PlantEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val hindiName: String,
    val category: String,
    val price: Double,
    val rating: Float,
    val description: String,
    val careLevel: String,
    val sunlight: String,
    val waterDays: Int,
    val imageResName: String,
    val isPopular: Boolean = false,
    val isFavorite: Boolean = false
)

@Entity(tableName = "garden_plants")
data class GardenPlantEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val species: String,
    val dateAdded: String,
    val lastWateredTimestamp: Long,
    val waterIntervalDays: Int,
    val notes: String = "",
    val healthStatus: String = "Healthy"
)

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val plantName: String,
    val issueName: String,
    val confidence: String,
    val diagnosis: String,
    val remedies: String,
    val scanDate: String,
    val imageType: String
)
