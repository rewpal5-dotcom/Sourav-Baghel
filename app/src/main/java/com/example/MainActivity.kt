package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.PlantEntity
import com.example.ui.components.CartBottomSheet
import com.example.ui.components.PlantDetailDialog
import com.example.ui.screens.*
import com.example.ui.theme.PaudPanditTheme
import com.example.ui.viewmodel.PlantViewModel

enum class NavigationTab(val route: String, val title: String, val icon: ImageVector) {
    HOME("home", "Home", Icons.Default.Home),
    DOCTOR("doctor", "AI Doctor", Icons.Default.MedicalServices),
    NURSERY("nursery", "Nursery", Icons.Default.LocalFlorist),
    GARDEN("garden", "My Garden", Icons.Default.Spa),
    CHAT("chat", "Pandit AI", Icons.Default.Chat)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaudPanditTheme {
                PaudPanditApp()
            }
        }
    }
}

@Composable
fun PaudPanditApp(viewModel: PlantViewModel = viewModel()) {
    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }
    var showCartSheet by remember { mutableStateOf(false) }

    val nurseryPlants by viewModel.nurseryPlants.collectAsStateWithLifecycle()
    val gardenPlants by viewModel.gardenPlants.collectAsStateWithLifecycle()
    val scanHistory by viewModel.scanHistory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val diagnosisState by viewModel.diagnosisState.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    val selectedPlantForDetail by viewModel.selectedPlantForDetail.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        modifier = Modifier.testTag("nav_item_${tab.route}")
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationTab.HOME -> HomeScreen(
                    nurseryPlants = nurseryPlants,
                    cartItems = cartItems,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.searchQuery.value = it },
                    selectedCategory = selectedCategory,
                    onCategorySelect = { viewModel.selectedCategory.value = it },
                    onNavigateToDoctor = { currentTab = NavigationTab.DOCTOR },
                    onNavigateToNursery = { currentTab = NavigationTab.NURSERY },
                    onNavigateToGarden = { currentTab = NavigationTab.GARDEN },
                    onNavigateToChat = { currentTab = NavigationTab.CHAT },
                    onPlantClick = { viewModel.selectedPlantForDetail.value = it },
                    onAddToCart = { viewModel.addToCart(it) },
                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                    onOpenCart = { showCartSheet = true }
                )

                NavigationTab.DOCTOR -> PlantDoctorScreen(
                    diagnosisState = diagnosisState,
                    scanHistory = scanHistory,
                    onDiagnose = { plantType, symptoms, notes, base64Image ->
                        viewModel.diagnosePlant(plantType, symptoms, notes, base64Image)
                    },
                    onResetState = { viewModel.resetDiagnosisState() }
                )

                NavigationTab.NURSERY -> NurseryScreen(
                    nurseryPlants = nurseryPlants,
                    cartItems = cartItems,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.searchQuery.value = it },
                    selectedCategory = selectedCategory,
                    onCategorySelect = { viewModel.selectedCategory.value = it },
                    onPlantClick = { viewModel.selectedPlantForDetail.value = it },
                    onAddToCart = { viewModel.addToCart(it) },
                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                    onOpenCart = { showCartSheet = true }
                )

                NavigationTab.GARDEN -> MyGardenScreen(
                    gardenPlants = gardenPlants,
                    onWaterPlant = { viewModel.recordWatering(it) },
                    onAddPlant = { name, species, interval, notes ->
                        viewModel.addPlantToGarden(name, species, interval, notes)
                    },
                    onDeletePlant = { viewModel.deleteGardenPlant(it) }
                )

                NavigationTab.CHAT -> PanditChatScreen(
                    messages = chatMessages,
                    isLoading = isChatLoading,
                    onSendMessage = { viewModel.sendChatMessage(it) }
                )
            }
        }
    }

    // Plant Care Detail Dialog
    selectedPlantForDetail?.let { plant ->
        PlantDetailDialog(
            plant = plant,
            onDismiss = { viewModel.selectedPlantForDetail.value = null },
            onAddToCart = { viewModel.addToCart(plant) }
        )
    }

    // Cart Bottom Sheet
    if (showCartSheet) {
        CartBottomSheet(
            cartItems = cartItems,
            onUpdateQuantity = { plantId, delta -> viewModel.updateCartQuantity(plantId, delta) },
            onRemoveItem = { plantId -> viewModel.removeFromCart(plantId) },
            onClearCart = { viewModel.clearCart() },
            onDismiss = { showCartSheet = false }
        )
    }
}
