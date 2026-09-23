package com.organistaapp.ui.screens.igrejas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.organistaapp.data.model.Igreja
import com.organistaapp.data.repository.IgrejaRepository
import com.organistaapp.data.repository.OrganistaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IgrejasUiState(
    val igrejas: List<Igreja> = emptyList(),
    val igrejasArquivadas: List<Igreja> = emptyList(),
    val isLoading: Boolean = true,
    val organistaId: Long = 0
)

@HiltViewModel
class IgrejasViewModel @Inject constructor(
    private val organistaRepository: OrganistaRepository,
    private val igrejaRepository: IgrejaRepository
) : ViewModel() {

    val uiState: StateFlow<IgrejasUiState> = organistaRepository.getOrganistaPrincipal()
        .flatMapLatest { organista ->
            if (organista == null) flowOf(IgrejasUiState(isLoading = false))
            else combine(
                igrejaRepository.getIgrejasAtivas(organista.id),
                igrejaRepository.getIgrejasArquivadas(organista.id)
            ) { ativas, arquivadas ->
                IgrejasUiState(
                    igrejas = ativas,
                    igrejasArquivadas = arquivadas,
                    isLoading = false,
                    organistaId = organista.id
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), IgrejasUiState())

    fun salvarIgreja(
        nome: String,
        endereco: String,
        diaCulto: String,
        horario: String,
        emRodizio: Boolean
    ) {
        viewModelScope.launch {
            val orgId = uiState.value.organistaId
            if (orgId == 0L) return@launch
            igrejaRepository.saveIgreja(
                Igreja(
                    organistaId = orgId,
                    nome = nome.trim(),
                    endereco = endereco.trim(),
                    diaCulto = diaCulto,
                    horarioPadrao = horario,
                    emRodizio = emRodizio
                )
            )
        }
    }

    fun atualizarIgreja(
        igreja: Igreja,
        nome: String,
        endereco: String,
        diaCulto: String,
        horario: String,
        emRodizio: Boolean
    ) {
        viewModelScope.launch {
            igrejaRepository.updateIgreja(
                igreja.copy(
                    nome = nome.trim(),
                    endereco = endereco.trim(),
                    diaCulto = diaCulto,
                    horarioPadrao = horario,
                    emRodizio = emRodizio
                )
            )
        }
    }

    fun arquivarIgreja(igreja: Igreja) {
        viewModelScope.launch { igrejaRepository.arquivarIgreja(igreja.id) }
    }

    fun desarquivarIgreja(igreja: Igreja) {
        viewModelScope.launch { igrejaRepository.desarquivarIgreja(igreja.id) }
    }

    fun deletarIgreja(igreja: Igreja) {
        viewModelScope.launch { igrejaRepository.deleteIgreja(igreja) }
    }
}
