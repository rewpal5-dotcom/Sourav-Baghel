package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.OrderEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Order history — reads the real rows saved in the local database. */
@Composable
fun OrdersDialog(
    orders: List<OrderEntity>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Band karein") }
        },
        title = {
            Text("Mere Orders", fontWeight = FontWeight.Bold)
        },
        text = {
            if (orders.isEmpty()) {
                Text("Abhi koi order nahi hai. Nursery se paudhe cart mein daal kar checkout karein.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    orders.take(20).forEach { order ->
                        Card(shape = RoundedCornerShape(12.dp)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = order.orderCode,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "₹${order.amount.toInt()}",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = order.itemsSummary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${order.paymentMode} • ${statusLabel(order.status)} • ${formatDate(order.createdAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

private fun statusLabel(status: String): String = when (status) {
    "PAYMENT_PENDING" -> "Payment pending"
    "PLACED" -> "Placed"
    else -> status
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(timestamp))
