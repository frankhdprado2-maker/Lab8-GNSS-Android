package com.lab.lab4.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lab.lab4.data.remote.NetworkConstants
import com.lab.lab4.data.remote.RetrofitClient
import com.lab.lab4.data.remote.model.GoogleLoginRequest
import com.lab.lab4.data.remote.model.LoginRequest
import com.lab.lab4.data.remote.model.RefreshTokenRequest
import com.lab.lab4.data.remote.model.RegisterRequest
import com.lab.lab4.data.session.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SessionViewModel(private val sessionManager: SessionManager) : ViewModel() {

    val isLoggedIn = sessionManager.isLoggedIn.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        false
    )

    val username = sessionManager.currentUsername.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    val isDarkMode = sessionManager.isDarkMode.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            sessionManager.setDarkMode(enabled)
        }
    }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.login(
                    NetworkConstants.PROJECT_SLUG,
                    LoginRequest(
                        email = email,
                        password = password,
                        deviceId = sessionManager.getDeviceId()
                    )
                )
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    sessionManager.login(email, body.accessToken, body.refreshToken)
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (_: Exception) {
                onResult(false)
            }
        }
    }

    fun register(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.register(
                    NetworkConstants.PROJECT_SLUG,
                    RegisterRequest(email = email, password = password)
                )
                onResult(response.isSuccessful)
            } catch (_: Exception) {
                onResult(false)
            }
        }
    }

    fun loginWithGoogle(googleToken: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.loginWithGoogle(
                    NetworkConstants.PROJECT_SLUG,
                    GoogleLoginRequest(
                        token = googleToken,
                        deviceId = sessionManager.getDeviceId()
                    )
                )
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    sessionManager.login("Google", body.accessToken, body.refreshToken)
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (_: Exception) {
                onResult(false)
            }
        }
    }

    fun refreshSession(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val currentRefreshToken = sessionManager.refreshToken.firstOrNull()
                if (currentRefreshToken.isNullOrBlank()) {
                    onResult(false)
                    return@launch
                }

                val response = RetrofitClient.apiService.refreshToken(
                    NetworkConstants.PROJECT_SLUG,
                    RefreshTokenRequest(
                        refreshToken = currentRefreshToken,
                        deviceId = sessionManager.getDeviceId()
                    )
                )
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    sessionManager.updateTokens(body.accessToken, body.refreshToken)
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (_: Exception) {
                onResult(false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.logout()
        }
    }

    class Factory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SessionViewModel(sessionManager) as T
        }
    }
}
