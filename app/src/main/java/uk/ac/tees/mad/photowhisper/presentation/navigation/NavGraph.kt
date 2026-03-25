package uk.ac.tees.mad.photowhisper.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import uk.ac.tees.mad.photowhisper.data.local.PreferencesManager
import uk.ac.tees.mad.photowhisper.data.remote.AuthService
import uk.ac.tees.mad.photowhisper.data.remote.SupabaseClient
import uk.ac.tees.mad.photowhisper.data.repository.AuthRepositoryImpl
import uk.ac.tees.mad.photowhisper.domain.usecase.GetCurrentUserUseCase
import uk.ac.tees.mad.photowhisper.domain.usecase.LoginUseCase
import uk.ac.tees.mad.photowhisper.domain.usecase.RegisterUseCase
import uk.ac.tees.mad.photowhisper.presentation.auth.login.LoginScreen
import uk.ac.tees.mad.photowhisper.presentation.auth.login.LoginViewModel
import uk.ac.tees.mad.photowhisper.presentation.auth.register.RegisterScreen
import uk.ac.tees.mad.photowhisper.presentation.auth.register.RegisterViewModel
import uk.ac.tees.mad.photowhisper.data.local.AppDatabase
import uk.ac.tees.mad.photowhisper.data.local.AudioPlayer
import uk.ac.tees.mad.photowhisper.data.local.AudioRecorder
import uk.ac.tees.mad.photowhisper.data.local.FileManager
import uk.ac.tees.mad.photowhisper.data.remote.DatabaseService
import uk.ac.tees.mad.photowhisper.data.remote.StorageService
import uk.ac.tees.mad.photowhisper.data.remote.SyncService
import uk.ac.tees.mad.photowhisper.data.repository.MemoryRepositoryImpl
import uk.ac.tees.mad.photowhisper.domain.usecase.GetMemoriesUseCase
import uk.ac.tees.mad.photowhisper.domain.usecase.LogoutUseCase
import uk.ac.tees.mad.photowhisper.domain.usecase.SaveMemoryUseCase
import uk.ac.tees.mad.photowhisper.presentation.capture.CaptureMemoryScreen
import uk.ac.tees.mad.photowhisper.presentation.capture.CaptureViewModel
import uk.ac.tees.mad.photowhisper.presentation.detail.MemoryDetailScreen
import uk.ac.tees.mad.photowhisper.presentation.detail.MemoryDetailViewModel
import uk.ac.tees.mad.photowhisper.presentation.home.HomeScreen
import uk.ac.tees.mad.photowhisper.presentation.home.HomeViewModel
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
            val loginUseCase = remember { LoginUseCase(authRepository) }
            val viewModel: LoginViewModel = viewModel {
                LoginViewModel(loginUseCase)
            }

            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            val registerUseCase = remember { RegisterUseCase(authRepository) }
            val viewModel: RegisterViewModel = viewModel {
                RegisterViewModel(registerUseCase)
            }

            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val database = remember { AppDatabase.getDatabase(context) }
            val fileManager = remember { FileManager(context) }
            val storageService = remember { StorageService(SupabaseClient) }
            val databaseService = remember { DatabaseService(SupabaseClient) }
            val syncService = remember { SyncService(storageService, databaseService, database.memoryDao(), fileManager) }
            val memoryRepository = remember { MemoryRepositoryImpl(database.memoryDao(), syncService) }
            val getMemoriesUseCase = remember { GetMemoriesUseCase(memoryRepository) }
            val getCurrentUserUseCase = remember { GetCurrentUserUseCase(authRepository) }
            val logoutUseCase = remember { LogoutUseCase(authRepository) }

            val viewModel: HomeViewModel = viewModel {
                HomeViewModel(getMemoriesUseCase, getCurrentUserUseCase, logoutUseCase, memoryRepository)
            }

            HomeScreen(
                viewModel = viewModel,
                onNavigateToCapture = {
                    navController.navigate(Screen.CaptureMemory.route)
                },
                onNavigateToDetail = { memoryId ->
                    navController.navigate(Screen.MemoryDetail.createRoute(memoryId))
                },
                onNavigateToAuth = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CaptureMemory.route) {
            val database = remember { AppDatabase.getDatabase(context) }
            val fileManager = remember { FileManager(context) }
            val storageService = remember { StorageService(SupabaseClient) }
            val databaseService = remember { DatabaseService(SupabaseClient) }
            val syncService = remember { SyncService(storageService, databaseService, database.memoryDao(), fileManager) }
            val memoryRepository = remember { MemoryRepositoryImpl(database.memoryDao(), syncService) }
            val saveMemoryUseCase = remember { SaveMemoryUseCase(memoryRepository) }
            val getCurrentUserUseCase = remember { GetCurrentUserUseCase(authRepository) }
            val audioRecorder = remember { AudioRecorder(context) }

            val viewModel: CaptureViewModel = viewModel {
                CaptureViewModel(saveMemoryUseCase, getCurrentUserUseCase, fileManager, audioRecorder)
            }

            CaptureMemoryScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.MemoryDetail.route,
            arguments = listOf(
                navArgument("memoryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val memoryId = backStackEntry.arguments?.getString("memoryId") ?: return@composable

            val database = remember { AppDatabase.getDatabase(context) }
            val fileManager = remember { FileManager(context) }
            val storageService = remember { StorageService(SupabaseClient) }
            val databaseService = remember { DatabaseService(SupabaseClient) }
            val syncService = remember { SyncService(storageService, databaseService, database.memoryDao(), fileManager) }
            val memoryRepository = remember { MemoryRepositoryImpl(database.memoryDao(), syncService) }
            val audioPlayer = remember { AudioPlayer(context) }

            val viewModel: MemoryDetailViewModel = viewModel(
                key = memoryId
            ) {
                MemoryDetailViewModel(memoryRepository, audioPlayer, memoryId)
            }

            MemoryDetailScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}