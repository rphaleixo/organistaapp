package com.organistaapp.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.organistaapp.data.model.Evento
import com.organistaapp.data.model.Organista
import com.organistaapp.data.repository.EventoRepository
import com.organistaapp.data.repository.OrganistaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class CalendarioUiState(
    val organista: Organista? = null,
    val mesSelecionado: YearMonth = YearMonth.now(),
    val eventosDoMes: List<Evento> = emptyList(),
    val isLoading: Boolean = true
) {
    val nomeOrganista: String get() = organista?.nome ?: ""
}

@HiltViewModel
class CalendarioViewModel @Inject constructor(
    private val organistaRepository: OrganistaRepository,
    private val eventoRepository: EventoRepository
) : ViewModel() {

    private val _mesSelecionado = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<CalendarioUiState> = combine(
        organistaRepository.getOrganistaPrincipal(),
        _mesSelecionado
    ) { organista, mes ->
        organista to mes
    }.flatMapLatest { (organista, mes) ->
        if (organista == null) {
            flowOf(CalendarioUiState(isLoading = false))
        } else {
            val inicio = mes.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val fim = mes.atEndOfMonth().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            eventoRepository.getEventosByMes(organista.id, inicio, fim).map { eventos ->
                CalendarioUiState(
                    organista = organista,
                    mesSelecionado = mes,
                    eventosDoMes = eventos,
                    isLoading = false
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarioUiState()
    )

    fun irParaMesAnterior() {
        _mesSelecionado.update { it.minusMonths(1) }
    }

    fun irParaProximoMes() {
        _mesSelecionado.update { it.plusMonths(1) }
    }

    fun selecionarMes(mes: YearMonth) {
        _mesSelecionado.value = mes
    }
}
