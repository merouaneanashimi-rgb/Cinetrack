package com.cinetrack.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.cinetrack.presentation.navigation.CineTrackNavigation
import com.cinetrack.presentation.screens.trakt.TraktViewModel
import com.cinetrack.presentation.theme.CineTrackTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val traktViewModel: TraktViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Handle OAuth redirect if app was opened via deep link
        handleIntent(intent)

        setContent {
            CineTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    CineTrackNavigation(navController = navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data = intent?.data ?: return
        // Handle cinetrack://trakt/oauth?code=XXXX
        if (data.scheme == "cinetrack" && data.host == "trakt") {
            val code = data.getQueryParameter("code")
            if (!code.isNullOrBlank()) {
                traktViewModel.handleOAuthCallback(code)
            }
        }
    }
}
