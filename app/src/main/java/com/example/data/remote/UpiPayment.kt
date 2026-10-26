package com.example.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.BuildConfig
import java.util.Locale

/**
 * Real money collection without a payment SDK: builds a standard UPI deep link
 * (upi://pay?...) which every UPI app on the phone (GPay, PhonePe, Paytm, BHIM, bank apps)
 * understands. The customer pays straight into the nursery's UPI ID.
 *
 * Configure it in .env:
 *   UPI_VPA=yournursery@okicici
 *   UPI_PAYEE_NAME=Paud Pandit Nursery
 */
object UpiPayment {

    fun isConfigured(): Boolean {
        val vpa = BuildConfig.UPI_VPA
        return vpa.isNotBlank() && vpa != "MY_UPI_VPA" && vpa.contains("@")
    }

    fun payeeName(): String =
        BuildConfig.UPI_PAYEE_NAME.ifBlank { "Paud Pandit Nursery" }

    fun buildPayIntent(amount: Double, orderCode: String): Intent {
        val uri = Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", BuildConfig.UPI_VPA.trim())
            .appendQueryParameter("pn", payeeName())
            .appendQueryParameter("tr", orderCode)
            .appendQueryParameter("tn", "Paud Pandit order $orderCode")
            .appendQueryParameter("am", String.format(Locale.US, "%.2f", amount))
            .appendQueryParameter("cu", "INR")
            .build()

        return Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /** Opens the UPI app chooser. Returns false when no UPI app is installed. */
    fun launch(context: Context, amount: Double, orderCode: String): Boolean {
        val intent = buildPayIntent(amount, orderCode)
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
