package com.organistaapp.ui.screens.profile

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes
import com.organistaapp.data.model.Organista
import com.organistaapp.data.repository.OrganistaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerfilUiState(
    val organista: Organista? = null,
    val isLoading: Boolean = true,
    val editando: Boolean = false,
    val nome: String = "",
    val email: String = "",
    val googleEmail: String = "",
    val mensagemSucesso: String? = null,
    val mensagemErro: String? = null
)

@HiltViewModel
class PerfilViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val organistaRepository: OrganistaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            organistaRepository.getOrganistaPrincipal().collect { organista ->
                _uiState.update { state ->
                    state.copy(
                        organista = organista,
                        nome = organista?.nome ?: "",
                        email = organista?.email ?: "",
                        googleEmail = organista?.googleAccountEmail ?: "",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun iniciarEdicao() {
        _uiState.update { it.copy(editando = true) }
    }

    fun cancelarEdicao() {
        val organista = _uiState.value.organista
        _uiState.update { it.copy(
            editando = false,
            nome = organista?.nome ?: "",
            email = organista?.email ?: ""
        )}
    }

    fun atualizarNome(nome: String) {
        _uiState.update { it.copy(nome = nome) }
    }

    fun atualizarEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun salvarPerfil() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.nome.isBlank()) {
                _uiState.update { it.copy(mensagemErro = "O nome não pode estar vazio.") }
                return@launch
            }
            val organista = state.organista?.copy(
                nome = state.nome.trim(),
                email = state.email.trim()
            ) ?: Organista(
                nome = state.nome.trim(),
                email = state.email.trim()
            )
            organistaRepository.saveOrganista(organista)
            _uiState.update { it.copy(
                editando = false,
                mensagemSucesso = "Perfil salvo com sucesso!"
            )}
        }
    }

    fun getGoogleSignInIntent(): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(CalendarScopes.CALENDAR))
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        return client.signInIntent
    }

    fun handleGoogleSignInResult(result: ActivityResult) {
        viewModelScope.launch {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.result
                val email = account.email ?: return@launch

                val organista = _uiState.value.organista
                if (organista != null) {
                    organistaRepository.updateOrganista(
                        organista.copy(googleAccountEmail = email)
                    )
                } else {
                    val nome = _uiState.value.nome.ifBlank { account.displayName ?: "" }
                    organistaRepository.saveOrganista(
                        Organista(nome = nome, googleAccountEmail = email)
                    )
                }
                _uiState.update { it.copy(
                    googleEmail = email,
                    mensagemSucesso = "Conta Google conectada: $email"
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    mensagemErro = "Erro ao conectar Google: ${e.message}"
                )}
            }
        }
    }

    fun desconectarGoogle() {
        viewModelScope.launch {
            val organista = _uiState.value.organista ?: return@launch
            organistaRepository.updateOrganista(organista.copy(googleAccountEmail = ""))
            _uiState.update { it.copy(googleEmail = "") }

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            GoogleSignIn.getClient(context, gso).signOut()
        }
    }

    fun limparMensagens() {
        _uiState.update { it.copy(mensagemSucesso = null, mensagemErro = null) }
    }
}
