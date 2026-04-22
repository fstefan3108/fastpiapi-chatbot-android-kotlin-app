package com.stefan.chatbotapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stefan.chatbotapp.navigation.Routes
import com.stefan.chatbotapp.ui.auth.LoginScreen
import com.stefan.chatbotapp.ui.auth.RegisterScreen
import com.stefan.chatbotapp.ui.chat.ChatScreen
import com.stefan.chatbotapp.ui.dashboard.DashboardScreen
import com.stefan.chatbotapp.ui.theme.ChatbotAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatbotAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToChat = { navController.navigate(Routes.CHAT) }
            )
        }

        composable(Routes.CHAT) {
            ChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}