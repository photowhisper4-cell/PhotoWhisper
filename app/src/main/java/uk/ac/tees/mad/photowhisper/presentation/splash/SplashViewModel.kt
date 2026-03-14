package uk.ac.tees.mad.photowhisper.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.photowhisper.domain.usecase.GetCurrentUserUseCase

class SplashViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Loading)
    val navigationState: StateFlow<NavigationState> = _navigationState

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            delay(2000)

            val currentUser = getCurrentUserUseCase()
            _navigationState.value = if (currentUser != null) {
                NavigationState.NavigateToHome
            } else {
                NavigationState.NavigateToAuth
            }
        }
    }

    sealed class NavigationState {
        object Loading : NavigationState()
        object NavigateToHome : NavigationState()
        object NavigateToAuth : NavigationState()
    }
}