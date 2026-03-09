package uk.ac.tees.mad.photowhisper.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object CaptureMemory : Screen("capture_memory")
    object MemoryDetail : Screen("memory_detail/{memoryId}") {
        fun createRoute(memoryId: String) = "memory_detail/$memoryId"
    }
    object Profile : Screen("profile")
}