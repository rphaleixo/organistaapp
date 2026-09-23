package com.organistaapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.organistaapp.data.model.Evento
import com.organistaapp.data.model.Organista
import com.organistaapp.data.repository.EventoRepository
import com.organistaapp.data.repository.OrganistaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class HomeUiState(
    val organista: Organista? = null,
    val proximosEventos: List<Evento> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val organistaRepository: OrganistaRepository,
    private val eventoRepository: EventoRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        organistaRepository.getOrganistaPrincipal(),
        eventoRepository.getProximosEventos(System.currentTimeMillis())
    ) { organista, eventos ->
        HomeUiState(
            organista = organista,
            proximosEventos = eventos.take(5),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
}
