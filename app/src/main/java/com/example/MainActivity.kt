package com.example

import android.os.Bundle
import android.webkit.CookieManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.AppViewModel
import com.example.ui.AppViewModelFactory
import com.example.ui.SettingsScreen
import com.example.ui.SpaceContainerScreen
import com.example.ui.SpacesScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    
    private val viewModel: AppViewModel by viewModels {
        AppViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePreference by viewModel.themePreference.collectAsState()
            MyApplicationTheme(themeSelection = themePreference) {
                AppNavigation(viewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val spaces by viewModel.spaces.collectAsState()
    val downloads by viewModel.downloads.collectAsState()
    val themePreference by viewModel.themePreference.collectAsState()
    val storagePath by viewModel.storagePath.collectAsState()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "spaces") {
        composable("spaces") {
            SpacesScreen(
                spaces = spaces,
                onSpaceClick = { space ->
                    // Clear cookies to simulate a new "browser profile" for the space
                    CookieManager.getInstance().removeAllCookies(null)
                    CookieManager.getInstance().flush()
                    navController.navigate("space_detail/${space.id}/${space.name}")
                },
                onAddSpaceClick = { name, color, isBusiness ->
                    viewModel.addSpace(name, color, isBusiness)
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                currentTheme = themePreference,
                onThemeSelected = { viewModel.setTheme(it) },
                currentStoragePath = storagePath,
                onStoragePathSelected = { viewModel.setStoragePath(it) },
                onNavigateBack = { navController.popBackStack() },
                onClearHistory = {
                    viewModel.clearAllHistory()
                    Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                }
            )
        }
        composable(
            route = "space_detail/{spaceId}/{spaceName}",
            arguments = listOf(
                navArgument("spaceId") { type = NavType.IntType },
                navArgument("spaceName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getInt("spaceId") ?: 0
            val spaceName = backStackEntry.arguments?.getString("spaceName") ?: ""
            
            val history by viewModel.getHistoryForSpace(spaceId).collectAsState(initial = emptyList())
            val currentSummary by viewModel.currentSummary.collectAsState()
            
            SpaceContainerScreen(
                spaceName = spaceName,
                spaceId = spaceId,
                downloads = downloads,
                history = history,
                onNavigateBack = { navController.popBackStack() },
                onDownloadRequested = { url, fileName ->
                    viewModel.addDownload(spaceId, fileName, url)
                    Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
                },
                onHistoryUpdate = { url, title ->
                    viewModel.addHistory(spaceId, url, title)
                }
            )
        }
    }
}
