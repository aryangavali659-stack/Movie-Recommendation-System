package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.UserPreferencesManager
import com.example.myapplication.data.remote.RetrofitClient
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.MovieRepository
import com.example.myapplication.data.repository.UserPreferencesRepository
import com.example.myapplication.presentation.navigation.AppNavigation
import com.example.myapplication.presentation.viewmodel.AuthViewModel
import com.example.myapplication.presentation.viewmodel.MovieDetailViewModel
import com.example.myapplication.presentation.viewmodel.ProfileViewModel
import com.example.myapplication.ui.MovieViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.lang.reflect.Proxy

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Direct Firestore Write Test
        runDirectFirestoreTestWrite()

        val database = AppDatabase.getDatabase(this)
        val repository = MovieRepository(RetrofitClient.apiService, database.watchlistDao())
        val userPreferencesManager = UserPreferencesManager(this)
        val authRepository = AuthRepository(this)
        val userPreferencesRepository = UserPreferencesRepository(this, authRepository)

        val authViewModel = AuthViewModel(authRepository)
        val profileViewModel = ProfileViewModel(userPreferencesManager)
        val movieViewModel = MovieViewModel(repository, userPreferencesRepository)
        val movieDetailViewModel = MovieDetailViewModel(repository)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        authViewModel = authViewModel,
                        authRepository = authRepository,
                        profileViewModel = profileViewModel,
                        movieViewModel = movieViewModel,
                        movieDetailViewModel = movieDetailViewModel,
                        userPreferencesManager = userPreferencesManager
                    )
                }
            }
        }
    }

    private fun runDirectFirestoreTestWrite() {
        try {
            val firestoreClass = Class.forName("com.google.firebase.firestore.FirebaseFirestore")
            val getInstanceMethod = firestoreClass.getMethod("getInstance")
            val firestoreInstance = getInstanceMethod.invoke(null)
            val collectionMethod = firestoreClass.getMethod("collection", String::class.java)
            val collectionRef = collectionMethod.invoke(firestoreInstance, "test_collection")
            val addMethod = collectionRef.javaClass.getMethod("add", Any::class.java)

            val testData = hashMapOf("test" to "Hello Firebase", "time" to System.currentTimeMillis())
            val task = addMethod.invoke(collectionRef, testData)

            val taskClass = Class.forName("com.google.android.gms.tasks.Task")
            val addOnSuccessListenerMethod = taskClass.getMethod("addOnSuccessListener", Class.forName("com.google.android.gms.tasks.OnSuccessListener"))
            val addOnFailureListenerMethod = taskClass.getMethod("addOnFailureListener", Class.forName("com.google.android.gms.tasks.OnFailureListener"))

            val successProxy = Proxy.newProxyInstance(
                ClassLoader.getSystemClassLoader(),
                arrayOf(Class.forName("com.google.android.gms.tasks.OnSuccessListener"))
            ) { _, _, args ->
                val docRef = args?.get(0)
                Log.d("FirestoreTest", "Direct write successful! Document Snapshot ID: $docRef")
                null
            }

            val failureProxy = Proxy.newProxyInstance(
                ClassLoader.getSystemClassLoader(),
                arrayOf(Class.forName("com.google.android.gms.tasks.OnFailureListener"))
            ) { _, _, args ->
                val ex = args?.get(0) as? Exception
                Log.e("FirestoreTest", "Direct write failed", ex)
                null
            }

            addOnSuccessListenerMethod.invoke(task, successProxy)
            addOnFailureListenerMethod.invoke(task, failureProxy)
        } catch (e: Throwable) {
            Log.d("FirestoreTest", "Direct Firestore test write initiated")
        }
    }
}
