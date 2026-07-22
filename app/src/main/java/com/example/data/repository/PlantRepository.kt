package com.example.data.repository

import com.example.data.local.GardenPlantEntity
import com.example.data.local.PlantDao
import com.example.data.local.PlantEntity
import com.example.data.local.ScanHistoryEntity
import com.example.data.remote.ChatMessage
import com.example.data.remote.GeminiClient
import com.example.data.remote.PlantDiagnosisResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PlantRepository(private val plantDao: PlantDao) {

    val allNurseryPlants: Flow<List<PlantEntity>> = plantDao.getAllNurseryPlants()
    val popularPlants: Flow<List<PlantEntity>> = plantDao.getPopularPlants()
    val gardenPlants: Flow<List<GardenPlantEntity>> = plantDao.getAllGardenPlants()
    val scanHistory: Flow<List<ScanHistoryEntity>> = plantDao.getScanHistory()

    suspend fun prepopulateNurseryIfEmpty() {
        val currentPlants = plantDao.getAllNurseryPlants().first()
        if (currentPlants.isEmpty()) {
            val samplePlants = listOf(
                PlantEntity(
                    name = "Monstera Deliciosa",
                    hindiName = "Swiss Cheese Plant",
                    category = "Indoor",
                    price = 499.0,
                    rating = 4.8f,
                    description = "Iconic tropical indoor plant with large split leaves. Air purifying and easy to grow.",
                    careLevel = "Easy",
                    sunlight = "Bright Indirect",
                    waterDays = 7,
                    imageResName = "img_indoor_plants",
                    isPopular = true
                ),
                PlantEntity(
                    name = "Snake Plant (Sansevieria)",
                    hindiName = "Nag Phani Paudha",
                    category = "Air Purifying",
                    price = 299.0,
                    rating = 4.9f,
                    description = "Filters airborne toxins 24/7. Survives in low light and requires very minimal watering.",
                    careLevel = "Easy",
                    sunlight = "Low Light / Any",
                    waterDays = 14,
                    imageResName = "img_indoor_plants",
                    isPopular = true
                ),
                PlantEntity(
                    name = "Peace Lily (Spathiphyllum)",
                    hindiName = "Shanti Paudha",
                    category = "Flowering",
                    price = 349.0,
                    rating = 4.7f,
                    description = "Elegant white blossoms and dark green foliage. Tells you when it needs water by slightly drooping.",
                    careLevel = "Medium",
                    sunlight = "Medium Indirect",
                    waterDays = 4,
                    imageResName = "img_indoor_plants",
                    isPopular = true
                ),
                PlantEntity(
                    name = "Tulsi (Holy Basil)",
                    hindiName = "Pavitra Tulsi",
                    category = "Medicinal",
                    price = 149.0,
                    rating = 5.0f,
                    description = "Sacred Indian medicinal herb rich in antioxidants. Boosts immunity and purifies air.",
                    careLevel = "Easy",
                    sunlight = "Direct Sunlight",
                    waterDays = 2,
                    imageResName = "img_plant_hero",
                    isPopular = true
                ),
                PlantEntity(
                    name = "Aloe Vera",
                    hindiName = "Grit Kumari",
                    category = "Medicinal",
                    price = 199.0,
                    rating = 4.8f,
                    description = "Succulent plant with soothing gel for skin and hair. Requires minimal care and bright light.",
                    careLevel = "Easy",
                    sunlight = "Bright Direct",
                    waterDays = 10,
                    imageResName = "img_indoor_plants",
                    isPopular = false
                ),
                PlantEntity(
                    name = "Areca Palm",
                    hindiName = "Subh Palm",
                    category = "Indoor",
                    price = 599.0,
                    rating = 4.6f,
                    description = "Feathery tropical fronds that increase indoor humidity and absorb carbon monoxide.",
                    careLevel = "Medium",
                    sunlight = "Bright Indirect",
                    waterDays = 5,
                    imageResName = "img_plant_hero",
                    isPopular = true
                ),
                PlantEntity(
                    name = "Rose Plant (Desi Gulab)",
                    hindiName = "Gulab Paudha",
                    category = "Flowering",
                    price = 249.0,
                    rating = 4.7f,
                    description = "Fragrant red blooms perfect for gardens, balconies, and home offerings.",
                    careLevel = "Medium",
                    sunlight = "Full Sunlight",
                    waterDays = 3,
                    imageResName = "img_indoor_plants",
                    isPopular = false
                ),
                PlantEntity(
                    name = "Jade Plant (Crassula Ovata)",
                    hindiName = "Kubera / Money Plant",
                    category = "Succulent",
                    price = 279.0,
                    rating = 4.9f,
                    description = "Vastu and Feng Shui symbol of good luck and prosperity. Compact woody succulent.",
                    careLevel = "Easy",
                    sunlight = "Bright Sunlight",
                    waterDays = 10,
                    imageResName = "img_indoor_plants",
                    isPopular = true
                )
            )
            plantDao.insertNurseryPlants(samplePlants)
        }

        val currentGarden = plantDao.getAllGardenPlants().first()
        if (currentGarden.isEmpty()) {
            val sampleGarden = listOf(
                GardenPlantEntity(
                    name = "Balcony Monstera",
                    species = "Monstera Deliciosa",
                    dateAdded = "12 June 2026",
                    lastWateredTimestamp = System.currentTimeMillis() - (86400000L * 3), // 3 days ago
                    waterIntervalDays = 7,
                    notes = "Growing new split leaf! Added vermicompost on Sunday.",
                    healthStatus = "Healthy"
                ),
                GardenPlantEntity(
                    name = "Mandir Tulsi Ji",
                    species = "Holy Basil",
                    dateAdded = "01 May 2026",
                    lastWateredTimestamp = System.currentTimeMillis() - (86400000L * 1), // 1 day ago
                    waterIntervalDays = 2,
                    notes = "Morning watering routine. Healthy green leaves.",
                    healthStatus = "Healthy"
                ),
                GardenPlantEntity(
                    name = "Living Room Peace Lily",
                    species = "Spathiphyllum",
                    dateAdded = "20 April 2026",
                    lastWateredTimestamp = System.currentTimeMillis() - (86400000L * 5), // 5 days ago (needs water!)
                    waterIntervalDays = 4,
                    notes = "Leaf tips slightly drooping. Time to water today.",
                    healthStatus = "Needs Attention"
                )
            )
            sampleGarden.forEach { plantDao.insertGardenPlant(it) }
        }
    }

    suspend fun toggleFavorite(plant: PlantEntity) {
        plantDao.updatePlant(plant.copy(isFavorite = !plant.isFavorite))
    }

    suspend fun addPlantToGarden(name: String, species: String, intervalDays: Int, notes: String) {
        val newPlant = GardenPlantEntity(
            name = name,
            species = species,
            dateAdded = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date()),
            lastWateredTimestamp = System.currentTimeMillis(),
            waterIntervalDays = intervalDays,
            notes = notes,
            healthStatus = "Healthy"
        )
        plantDao.insertGardenPlant(newPlant)
    }

    suspend fun recordWatering(plant: GardenPlantEntity) {
        val updated = plant.copy(
            lastWateredTimestamp = System.currentTimeMillis(),
            healthStatus = "Healthy"
        )
        plantDao.updateGardenPlant(updated)
    }

    suspend fun deleteGardenPlant(id: Int) {
        plantDao.deleteGardenPlant(id)
    }

    suspend fun runDiagnosis(
        plantType: String,
        symptoms: String,
        additionalInfo: String,
        base64Image: String? = null
    ): PlantDiagnosisResult {
        val result = GeminiClient.diagnosePlantIssue(plantType, symptoms, additionalInfo, base64Image)
        // Save scan to history
        val scanEntity = ScanHistoryEntity(
            plantName = result.plantName,
            issueName = result.conditionName,
            confidence = result.confidence,
            diagnosis = result.fullAnalysisMarkdown,
            remedies = result.remedySummary,
            scanDate = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date()),
            imageType = "leaf_scan"
        )
        plantDao.insertScanHistory(scanEntity)
        return result
    }

    suspend fun askChat(query: String, history: List<ChatMessage>): String {
        return GeminiClient.askPanditChat(query, history)
    }
}
