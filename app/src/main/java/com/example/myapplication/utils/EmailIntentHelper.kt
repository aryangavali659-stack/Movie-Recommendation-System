package com.example.myapplication.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

object EmailIntentHelper {

    fun sendNewReleaseEmail(
        context: Context,
        recipientEmail: String,
        movieTitle: String,
        releaseInfo: String,
        posterUrl: String = ""
    ) {
        val subject = "🎬 Exciting New Release: $movieTitle is Here!"
        val body = """
            Hello!
            
            Great news! A brand new title has just dropped on your Movie Recommendation app:
            
            📌 Title: $movieTitle
            🖼️ Poster View: ${posterUrl.ifBlank { "Available in App" }}
            📝 Details: $releaseInfo
            
            Open the app now to check out the details, view the poster, and add it to your watchlist!
            
            Best regards,
            Movie Recommendation Team
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            val chooser = Intent.createChooser(intent, "Send email via...")
            context.startActivity(chooser)
            Log.d("EmailDebug", "Email intent launched successfully.")
        } catch (e: Exception) {
            Log.e("EmailDebug", "Failed to launch email intent", e)
        }
    }
}
