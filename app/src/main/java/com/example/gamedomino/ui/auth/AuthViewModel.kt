package com.example.gamedomino.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamedomino.data.model.User
import com.example.gamedomino.data.repository.AuthRepository
import com.example.gamedomino.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona la lógica de estado para Login y Registro.
 */
class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    // Estado reactivo para el proceso de Login
    private val _loginState = MutableStateFlow<Resource<User>?>(null)
    val loginState: StateFlow<Resource<User>?> = _loginState.asStateFlow()

    // Estado reactivo para el proceso de Registro
    private val _registerState = MutableStateFlow<Resource<User>?>(null)
    val registerState: StateFlow<Resource<User>?> = _registerState.asStateFlow()

    /**
     * Verifica si hay una sesión activa de usuario.
     */
    fun isUserLoggedIn(): Boolean {
        return repository.getCurrentUser() != null
    }

    /**
     * Inicia sesión llamando al repositorio.
     */
    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _loginState.value = Resource.Error("Por favor completa todos los campos")
            return
        }

        viewModelScope.launch {
            _loginState.value = Resource.Loading
            _loginState.value = repository.loginUser(email, pass)
        }
    }


    fun register(email: String, pass: String, username: String) {
        if (email.isBlank() || pass.isBlank() || username.isBlank()) {
            _registerState.value = Resource.Error("Por favor completa todos los campos")
            return
        }

        viewModelScope.launch {
            _registerState.value = Resource.Loading
            _registerState.value = repository.registerUser(email, pass, username)
        }
    }


    fun resetStates() {
        _loginState.value = null
        _registerState.value = null
    }


    fun logout() {
        repository.logout()
        resetStates()
    }
}