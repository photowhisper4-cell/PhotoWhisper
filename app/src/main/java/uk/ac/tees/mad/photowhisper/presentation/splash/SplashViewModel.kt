package uk.ac.tees.mad.photowhisper.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uk.ac.tees.mad.photowhisper.data.local.PreferencesManager
import uk.ac.tees.mad.photowhisper.domain.usecase.GetCurrentUserUseCase

class SplashViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Loading)
    val navigationState: StateFlow<NavigationState> = _navigationState

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            delay(2000)

            try {
                val isLoggedIn = preferencesManager.isLoggedIn.first()

                if (isLoggedIn) {
                    val currentUser = getCurrentUserUseCase()
                    _navigationState.value = if (currentUser != null) {
                        NavigationState.NavigateToHome
                    } else {
                        NavigationState.NavigateToAuth
                    }
                } else {
                    _navigationState.value = NavigationState.NavigateToAuth
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _navigationState.value = NavigationState.NavigateToAuth
            }
        }
    }

    sealed class NavigationState {
        object Loading : NavigationState()
        object NavigateToHome : NavigationState()
        object NavigateToAuth : NavigationState()
    }
}