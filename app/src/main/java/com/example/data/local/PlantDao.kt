package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {
    // Nursery Plants Queries
    @Query("SELECT * FROM nursery_plants ORDER BY id ASC")
    fun getAllNurseryPlants(): Flow<List<PlantEntity>>

    @Query("SELECT * FROM nursery_plants WHERE isPopular = 1")
    fun getPopularPlants(): Flow<List<PlantEntity>>

    @Query("SELECT * FROM nursery_plants WHERE category = :category")
    fun getPlantsByCategory(category: String): Flow<List<PlantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNurseryPlants(plants: List<PlantEntity>)

    @Update
    suspend fun updatePlant(plant: PlantEntity)

    // Garden Plants Queries
    @Query("SELECT * FROM garden_plants ORDER BY lastWateredTimestamp ASC")
    fun getAllGardenPlants(): Flow<List<GardenPlantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGardenPlant(plant: GardenPlantEntity)

    @Update
    suspend fun updateGardenPlant(plant: GardenPlantEntity)

    @Query("DELETE FROM garden_plants WHERE id = :id")
    suspend fun deleteGardenPlant(id: Int)

    // Scan History Queries
    @Query("SELECT * FROM scan_history ORDER BY id DESC")
    fun getScanHistory(): Flow<List<ScanHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanHistory(scan: ScanHistoryEntity)
}
