package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.cinestream.data.analytics.AnalyticsManager
import com.example.cinestream.ui.navigation.MainNavGraph
import com.example.cinestream.ui.theme.CineStreamTheme
import com.example.cinestream.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase Analytics
        AnalyticsManager.initialize(applicationContext)

        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val themeMode by mainViewModel.themeMode.collectAsState()

            CineStreamTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                MainNavGraph(
                    navController = navController,
                    viewModel = mainViewModel
                )
            }
        }
    }
}
