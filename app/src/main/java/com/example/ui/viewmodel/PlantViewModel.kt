package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.GardenPlantEntity
import com.example.data.local.PlantEntity
import com.example.data.local.ScanHistoryEntity
import com.example.data.remote.ChatMessage
import com.example.data.remote.PlantDiagnosisResult
import com.example.data.repository.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartItem(
    val plant: PlantEntity,
    val quantity: Int = 1
)

sealed interface DiagnosisUiState {
    object Idle : DiagnosisUiState
    object Loading : DiagnosisUiState
    data class Success(val result: PlantDiagnosisResult) : DiagnosisUiState
    data class Error(val message: String) : DiagnosisUiState
}

class PlantViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PlantRepository
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")

    val nurseryPlants: StateFlow<List<PlantEntity>>
    val gardenPlants: StateFlow<List<GardenPlantEntity>>
    val scanHistory: StateFlow<List<ScanHistoryEntity>>

    val cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val diagnosisState = MutableStateFlow<DiagnosisUiState>(DiagnosisUiState.Idle)

    val selectedLanguage = MutableStateFlow("hi") // "hi" for Hindi default, "en" for English

    val chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                message = "नमस्ते जी! 🌿 मैं हूँ आपका पौध पंडित (Paud Pandit)। आज आपके बगीचे और पौधों के लिए क्या सहायता कर सकता हूँ? मुझसे कोई भी सवाल पूछें या पत्ते की बीमारी जांचें!",
                isUser = false
            )
        )
    )
    val isChatLoading = MutableStateFlow(false)
    val selectedPlantForDetail = MutableStateFlow<PlantEntity?>(null)

    init {
        val dao = AppDatabase.getDatabase(application).plantDao()
        repository = PlantRepository(dao)

        viewModelScope.launch {
            repository.prepopulateNurseryIfEmpty()
        }

        nurseryPlants = combine(
            repository.allNurseryPlants,
            searchQuery,
            selectedCategory
        ) { plants, query, category ->
            plants.filter { plant ->
                val matchesQuery = plant.name.contains(query, ignoreCase = true) ||
                        plant.hindiName.contains(query, ignoreCase = true) ||
                        plant.category.contains(query, ignoreCase = true)
                val matchesCategory = category == "All" || plant.category.equals(category, ignoreCase = true)
                matchesQuery && matchesCategory
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        gardenPlants = repository.gardenPlants.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        scanHistory = repository.scanHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun toggleFavorite(plant: PlantEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(plant)
        }
    }

    fun addToCart(plant: PlantEntity) {
        val currentList = cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.plant.id == plant.id }
        if (index >= 0) {
            val existing = currentList[index]
            currentList[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentList.add(CartItem(plant = plant, quantity = 1))
        }
        cartItems.value = currentList
    }

    fun removeFromCart(plantId: Int) {
        cartItems.value = cartItems.value.filter { it.plant.id != plantId }
    }

    fun clearCart() {
        cartItems.value = emptyList()
    }

    fun updateCartQuantity(plantId: Int, delta: Int) {
        val currentList = cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.plant.id == plantId }
        if (index >= 0) {
            val existing = currentList[index]
            val newQty = existing.quantity + delta
            if (newQty <= 0) {
                currentList.removeAt(index)
            } else {
                currentList[index] = existing.copy(quantity = newQty)
            }
            cartItems.value = currentList
        }
    }

    fun addPlantToGarden(name: String, species: String, intervalDays: Int, notes: String) {
        viewModelScope.launch {
            repository.addPlantToGarden(name, species, intervalDays, notes)
        }
    }

    fun recordWatering(plant: GardenPlantEntity) {
        viewModelScope.launch {
            repository.recordWatering(plant)
        }
    }

    fun toggleLanguage() {
        selectedLanguage.value = if (selectedLanguage.value == "hi") "en" else "hi"
    }

    fun deleteGardenPlant(id: Int) {
        viewModelScope.launch {
            repository.deleteGardenPlant(id)
        }
    }

    fun diagnosePlant(plantType: String, symptoms: String, notes: String, base64Image: String? = null) {
        viewModelScope.launch {
            diagnosisState.value = DiagnosisUiState.Loading
            try {
                val result = repository.runDiagnosis(plantType, symptoms, notes, base64Image)
                diagnosisState.value = DiagnosisUiState.Success(result)
            } catch (e: Exception) {
                diagnosisState.value = DiagnosisUiState.Error(e.message ?: "Diagnosis failed")
            }
        }
    }

    fun resetDiagnosisState() {
        diagnosisState.value = DiagnosisUiState.Idle
    }

    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val userMsg = ChatMessage(message = userText, isUser = true)
        val updatedList = chatMessages.value + userMsg
        chatMessages.value = updatedList
        isChatLoading.value = true

        viewModelScope.launch {
            try {
                val replyText = repository.askChat(userText, updatedList)
                val botMsg = ChatMessage(message = replyText, isUser = false)
                chatMessages.value = chatMessages.value + botMsg
            } catch (e: Exception) {
                val botMsg = ChatMessage(message = "Apologies, I couldn't process that right now. Please check your connection and try again!", isUser = false)
                chatMessages.value = chatMessages.value + botMsg
            } finally {
                isChatLoading.value = false
            }
        }
    }
}
