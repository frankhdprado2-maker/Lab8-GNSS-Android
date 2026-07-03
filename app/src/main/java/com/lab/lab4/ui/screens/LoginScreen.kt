package com.lab.lab4.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.lab.lab4.data.remote.NetworkConstants
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onSubmit: (username: String, password: String, onResult: (Boolean) -> Unit) -> Unit,
    onGoogleLogin: (token: String, onResult: (Boolean) -> Unit) -> Unit,
    onRegisterNavigate: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var verificando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun handleGoogleLogin() {
        scope.launch {
            try {
                error = null

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(NetworkConstants.GOOGLE_WEB_CLIENT_ID)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                val credential = result.credential

                if (
                    credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)

                    val idToken = googleIdTokenCredential.idToken

                    verificando = true
                    onGoogleLogin(idToken) { success ->
                        verificando = false
                        if (!success) {
                            error = "Error al autenticar con Google"
                        }
                    }
                } else {
                    error = "Credencial de Google no valida"
                }
            } catch (e: Exception) {
                verificando = false
                error = "Cancelado o error: ${e.message}"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Lab 7",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Usa tus credenciales de Platform API",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        error = null
                    },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        error = null
                    },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Ocultar contrasena" else "Mostrar contrasena"
                            )
                        }
                    },
                    label = { Text("Contrasena") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    isError = error != null,
                    supportingText = {
                        error?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            error = "Ingresa email y contrasena."
                            return@Button
                        }
                        verificando = true
                        onSubmit(email.trim(), password) { success ->
                            verificando = false
                            if (!success) {
                                error = "Credenciales incorrectas. Revisa tu email y contrasena."
                            }
                        }
                    },
                    enabled = !verificando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (verificando) "Ingresando..." else "Ingresar")
                }

                OutlinedButton(
                    onClick = { handleGoogleLogin() },
                    enabled = !verificando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Continuar con Google")
                }

                OutlinedButton(
                    onClick = onRegisterNavigate,
                    enabled = !verificando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar usuario")
                }
            }
        }
    }
}
