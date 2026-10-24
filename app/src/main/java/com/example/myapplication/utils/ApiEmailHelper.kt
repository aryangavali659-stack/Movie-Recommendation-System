package com.example.myapplication.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object ApiEmailHelper {

    private const val EMAIL_JS_URL = "https://api.emailjs.com/api/v1.0/email/send"

    private const val SERVICE_ID = "service_2b0gpbl"
    private const val TEMPLATE_ID = "template_rxm7ta4"
    private const val PUBLIC_KEY = "D1-cpFR6-7TjmwPYe"

    suspend fun sendAutomatedEmail(
        userEmail: String,
        movieTitle: String,
        releaseInfo: String,
        posterUrl: String
    ) {
        Log.d("ApiEmail", "Triggering email for: $movieTitle to $userEmail")

        withContext(Dispatchers.IO) {
            try {
                val url = URL(EMAIL_JS_URL)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    doOutput = true
                    doInput = true
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                val templateParams = JSONObject().apply {
                    put("to_email", userEmail)
                    put("movie_title", movieTitle)
                    put("release_info", releaseInfo)
                    put("poster_url", posterUrl)
                }

                val jsonPayload = JSONObject().apply {
                    put("service_id", SERVICE_ID)
                    put("template_id", TEMPLATE_ID)
                    put("user_id", PUBLIC_KEY)
                    put("template_params", templateParams)
                }

                val writer = OutputStreamWriter(connection.outputStream, "UTF-8")
                writer.write(jsonPayload.toString())
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("ApiEmail", "Automated Email sent successfully via EmailJS!")
                } else {
                    val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e("ApiEmail", "Failed to send email. Code: $responseCode, Error: $errorStream")
                }
            } catch (e: Exception) {
                Log.e("ApiEmail", "Exception occurred while sending automated email: ${e.message}", e)
            }
        }
    }
}