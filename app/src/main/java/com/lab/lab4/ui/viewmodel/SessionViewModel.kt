package com.lab.lab4.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.lab4.data.session.SessionManager
import kotlinx.coroutines.flow.SharingStarted
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

    fun login(username: String) {
        viewModelScope.launch {
            sessionManager.login(username)
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.logout()
        }
    }
}