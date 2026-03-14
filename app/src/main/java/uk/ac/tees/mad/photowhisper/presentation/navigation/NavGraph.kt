package uk.ac.tees.mad.photowhisper.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import uk.ac.tees.mad.photowhisper.data.local.PreferencesManager
import uk.ac.tees.mad.photowhisper.data.remote.AuthService
import uk.ac.tees.mad.photowhisper.data.remote.SupabaseClient
import uk.ac.tees.mad.photowhisper.data.repository.AuthRepositoryImpl
import uk.ac.tees.mad.photowhisper.domain.usecase.GetCurrentUserUseCase


import uk.ac.tees.mad.photowhisper.presentation.splash.SplashScreen
import uk.ac.tees.mad.photowhisper.presentation.splash.SplashViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val authService = remember { AuthService(SupabaseClient) }
    val authRepository = remember { AuthRepositoryImpl(authService, preferencesManager) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            val getCurrentUserUseCase = remember { GetCurrentUserUseCase(authRepository) }
            val viewModel: SplashViewModel = viewModel {
                SplashViewModel(getCurrentUserUseCase)
            }

            SplashScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToAuth = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
//            val loginUseCase = remember { LoginUseCase(authRepository) }
//            val viewModel: LoginViewModel = viewModel {
//                LoginViewModel(loginUseCase)
//            }
//
//            LoginScreen(
//                viewModel = viewModel,
//                onNavigateToRegister = {
//                    navController.navigate(Screen.Register.route)
//                },
//                onNavigateToHome = {
//                    navController.navigate(Screen.Home.route) {
//                        popUpTo(Screen.Login.route) { inclusive = true }
//                    }
//                }
//            )
        }

        composable(Screen.Register.route) {
//            val registerUseCase = remember { RegisterUseCase(authRepository) }
//            val viewModel: RegisterViewModel = viewModel {
//                RegisterViewModel(registerUseCase)
//            }

//            RegisterScreen(
//                viewModel = viewModel,
//                onNavigateToLogin = {
//                    navController.popBackStack()
//                },
//                onNavigateToHome = {
//                    navController.navigate(Screen.Home.route) {
//                        popUpTo(Screen.Register.route) { inclusive = true }
//                    }
//                }
//            )
        }

        composable(Screen.Home.route) {
//            HomeScreen()
        }
    }
}