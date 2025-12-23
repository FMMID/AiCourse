package com.example.aicourse

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.aicourse.data.chat.local.ChatLocalDataSource.Companion.MAIN_CHAT_ID
import com.example.aicourse.data.notifications.PushTokenManager
import com.example.aicourse.mcpclient.UserSession
import com.example.aicourse.navigation.Screen
import com.example.aicourse.presentation.chat.ChatScreen
import com.example.aicourse.presentation.chat.mvi.ChatViewModel
import com.example.aicourse.presentation.settings.SettingsScreen
import com.example.aicourse.rag.presentation.RagScreen
import com.example.aicourse.rag.presentation.RagViewModel
import com.example.aicourse.ui.theme.AiCourseTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = UserSession.CURRENT_USER_ID
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }
        PushTokenManager.register(userId)

        enableEdgeToEdge()
        setContent {
            AiCourseTheme {
                AiCourseNavHost()
            }
        }
    }
}

@Composable
fun AiCourseNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.RagList // Стартуем с выбора базы (или Screen.Chat для теста)
    ) {
        // Экран списка RAG
        composable<Screen.RagList> {
            val viewModel: RagViewModel = koinViewModel()

            RagScreen(
                viewModel = viewModel,
                onIndexSelected = { indexId ->
                    navController.navigate(Screen.Chat(chatId = MAIN_CHAT_ID, ragIndexId = indexId))
                }
            )
        }

        // Экран Чата
        composable<Screen.Chat> { backStackEntry ->
            val route: Screen.Chat = backStackEntry.toRoute()

            // Инжектим ViewModel через Koin с параметрами
            val viewModel: ChatViewModel = koinViewModel {
                parametersOf(route.chatId, route.ragIndexId)
            }

            ChatScreen(
                viewModel = viewModel
            )
        }

        // Экран настроек
        composable<Screen.Settings> {
            SettingsScreen()
        }
    }
}