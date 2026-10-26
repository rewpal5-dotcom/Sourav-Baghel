package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.R
import com.example.data.remote.UpiPayment
import com.example.ui.viewmodel.CartItem

private enum class CheckoutStep { CART, DETAILS, DONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartBottomSheet(
    cartItems: List<CartItem>,
    onUpdateQuantity: (plantId: Int, delta: Int) -> Unit,
    onRemoveItem: (plantId: Int) -> Unit,
    onClearCart: () -> Unit,
    onPlaceOrder: (
        orderCode: String,
        address: String,
        phone: String,
        paymentMode: String,
        status: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var step by remember { mutableStateOf(CheckoutStep.CART) }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("UPI") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var placedCode by remember { mutableStateOf("") }
    var placedStatus by remember { mutableStateOf("") }

    val subtotal = cartItems.sumOf { it.plant.price * it.quantity }
    val deliveryFee = if (subtotal > 499 || subtotal == 0.0) 0.0 else 49.0
    val totalAmount = subtotal + deliveryFee

    // Real checkout: validates details, saves the order, then opens the UPI app.
    fun submitOrder() {
        errorMessage = null
        val cleanAddress = address.trim()
        val cleanPhone = phone.filter { it.isDigit() }

        if (cleanAddress.length < 10) {
            errorMessage = "Poora delivery address likhein (kam se kam 10 characters)."
            return
        }
        if (cleanPhone.length != 10) {
            errorMessage = "10 digit ka mobile number daalein."
            return
        }

        val orderCode = "PP" + System.currentTimeMillis().toString().takeLast(8)

        if (paymentMode == "UPI") {
            if (!UpiPayment.isConfigured()) {
                errorMessage = "UPI ID set nahi hai. Project ki .env file mein " +
                    "UPI_VPA=aapka@bank aur UPI_PAYEE_NAME=Paud Pandit Nursery daalein, " +
                    "phir app dobara build karein."
                return
            }
            val launched = UpiPayment.launch(context, totalAmount, orderCode)
            if (!launched) {
                errorMessage = "Is phone par koi UPI app nahi mila. Cash on Delivery chunein, " +
                    "ya pehle GPay / PhonePe / Paytm install karein."
                return
            }
            onPlaceOrder(orderCode, cleanAddress, cleanPhone, "UPI", "PAYMENT_PENDING")
            placedCode = orderCode
            placedStatus = "PAYMENT_PENDING"
            step = CheckoutStep.DONE
        } else {
            onPlaceOrder(orderCode, cleanAddress, cleanPhone, "COD", "PLACED")
            placedCode = orderCode
            placedStatus = "PLACED"
            step = CheckoutStep.DONE
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            when {
                // ------------------------------------------------ order placed
                step == CheckoutStep.DONE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (placedStatus == "PAYMENT_PENDING") {
                                "Order ban gaya — payment baaki hai 🪴"
                            } else {
                                "Order place ho gaya! 🪴"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Order ID: $placedCode",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (placedStatus == "PAYMENT_PENDING") {
                                "UPI app khul gaya hai — wahan payment complete karein. " +
                                    "Payment ke baad order nursery ko chala jayega."
                            } else {
                                "Cash on Delivery — delivery ke waqt payment karein. " +
                                    "Order 24-48 ghante mein pahunch jayega."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                onClearCart()
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back to Nursery")
                        }
                    }
                }

                // -------------------------------------------------- empty cart
                cartItems.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Empty Cart",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Your Nursery Cart is Empty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Explore our healthy indoor plants, pots, and soil mixes!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ----------------------------------------------- delivery details
                step == CheckoutStep.DETAILS -> {
                    Text(
                        text = "Delivery & Payment",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Delivery address") },
                        placeholder = { Text("Ghar / flat, street, city, pincode") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Payment method",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = paymentMode == "UPI",
                            onClick = { paymentMode = "UPI" }
                        )
                        Column {
                            Text("UPI — GPay / PhonePe / Paytm", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = if (UpiPayment.isConfigured()) {
                                    "Seedha nursery ke UPI ID par payment"
                                } else {
                                    "UPI ID set nahi hai (.env mein UPI_VPA daalein)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = paymentMode == "COD",
                            onClick = { paymentMode = "COD" }
                        )
                        Column {
                            Text("Cash on Delivery", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Delivery ke waqt payment",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val err = errorMessage
                    if (err != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { step = CheckoutStep.CART }) {
                            Text("Peeche")
                        }
                        Button(
                            onClick = { submitOrder() },
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (paymentMode == "UPI") {
                                    "UPI se pay karein ₹${totalAmount.toInt()}"
                                } else {
                                    "Order confirm karein ₹${totalAmount.toInt()}"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // --------------------------------------------------------- cart
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nursery Cart (${cartItems.sumOf { it.quantity }})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onClearCart) {
                            Text("Clear All", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(cartItems, key = { it.plant.id }) { item ->
                            val imageRes = when (item.plant.imageResName) {
                                "img_plant_hero" -> R.drawable.img_plant_hero
                                "img_indoor_plants" -> R.drawable.img_indoor_plants
                                else -> R.drawable.img_indoor_plants
                            }

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = imageRes),
                                        contentDescription = item.plant.name,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.plant.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "₹${item.plant.price.toInt()} each",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.background(
                                            MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    ) {
                                        IconButton(
                                            onClick = { onUpdateQuantity(item.plant.id, -1) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Decrease",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Text(
                                            text = "${item.quantity}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )

                                        IconButton(
                                            onClick = { onUpdateQuantity(item.plant.id, 1) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Increase",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { onRemoveItem(item.plant.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "₹${subtotal.toInt()}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Nursery Delivery", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = if (deliveryFee == 0.0) "FREE" else "₹${deliveryFee.toInt()}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (deliveryFee == 0.0) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.Unspecified
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Amount",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "₹${totalAmount.toInt()}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            errorMessage = null
                            step = CheckoutStep.DETAILS
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Checkout — ₹${totalAmount.toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
