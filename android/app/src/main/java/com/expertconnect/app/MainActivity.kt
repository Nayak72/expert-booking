package com.expertconnect.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.expertconnect.app.navigation.AppNavGraph
import com.expertconnect.app.ui.theme.ExpertConnectTheme
import com.expertconnect.app.utils.DataStoreManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main entry point for ExpertConnect.
 * Hosts the root AppNavGraph which routes to UserNavGraph or ExpertNavGraph
 * based on authenticated user role.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ExpertConnectTheme {
                AppNavGraph(dataStoreManager = dataStoreManager)
            }
        }
    }
}
