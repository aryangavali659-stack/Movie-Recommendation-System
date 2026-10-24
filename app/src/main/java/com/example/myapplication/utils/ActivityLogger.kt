package com.example.myapplication.utils

import android.util.Log
import com.example.myapplication.data.model.UserActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ActivityLogger {

    private val _activities = MutableStateFlow<List<UserActivity>>(emptyList())
    val activities: StateFlow<List<UserActivity>> = _activities.asStateFlow()

    fun logActivity(userId: String, userEmail: String, activityType: String, movieTitle: String) {
        logUserAction(userId = userId, activityType = activityType, movieTitle = movieTitle, email = userEmail)
    }

    fun logUserAction(userId: String, activityType: String, movieTitle: String = "", email: String = userId) {
        val safeUserId = userId.ifBlank { email }
        if (safeUserId.isBlank()) {
            Log.e("FirestoreDebug", "Error: Cannot log action, userId and email are empty")
            return
        }

        val activityData = hashMapOf(
            "userId" to safeUserId,
            "userEmail" to email,
            "activityType" to activityType,
            "movieTitle" to movieTitle,
            "timestamp" to System.currentTimeMillis()
        )

        val activity = UserActivity(
            userId = safeUserId,
            userEmail = email,
            activityType = activityType,
            movieTitle = movieTitle,
            timestamp = System.currentTimeMillis()
        )

        Log.d("FirestoreDebug", "Attempting to write activity for user: $safeUserId")

        // Direct Firebase Firestore SDK Call
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("user_activities")
                .add(activityData)
                .addOnSuccessListener { docRef ->
                    Log.d("FirestoreDebug", "SUCCESS: Written to Firestore with ID: ${docRef.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreDebug", "FAILURE: Could not write to Firestore", e)
                }
        } catch (e: Exception) {
            Log.e("FirestoreDebug", "Exception during Firestore write", e)
        }

        val currentList = _activities.value.toMutableList()
        currentList.add(0, activity)
        _activities.value = currentList
    }
}
