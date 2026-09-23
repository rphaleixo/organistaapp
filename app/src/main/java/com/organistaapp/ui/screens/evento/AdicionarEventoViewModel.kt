package com.organistaapp.ui.screens.evento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.organistaapp.data.model.Escala
import com.organistaapp.data.model.Evento
import com.organistaapp.data.model.Igreja
import com.organistaapp.data.repository.EscalaRepository
import com.organistaapp.data.repository.EventoRepository
import com.organistaapp.data.repository.IgrejaRepository
import com.organistaapp.data.repository.OrganistaRepository
import com.organistaapp.utils.GoogleCalendarUtils
import com.organistaapp.utils.NotificationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

data class AdicionarEventoUiState(
    val igrejas: List<Igreja> = emptyList(),
    val igrejaSelecionada: Igreja? = null,
    val nomeIgrejaManual: String = "",
    val data: LocalDate = LocalDate.now(),
    val hora: LocalTime = LocalTime.of(8, 0),
    val salvando: Boolean = false,
    val sucesso: Boolean = false,
    val erro: String? = null
)

@HiltViewModel
class AdicionarEventoViewModel @Inject constructor(
    private val organistaRepository: OrganistaRepository,
    private val igrejaRepository: IgrejaRepository,
    private val escalaRepository: EscalaRepository,
    private val eventoRepository: EventoRepository,
    private val calendarUtils: GoogleCalendarUtils,
    private val notificationUtils: NotificationUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdicionarEventoUiState())
    val uiState: StateFlow<AdicionarEventoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            organistaRepository.getOrganistaPrincipal().collect { organista ->
                organista?.let {
                    igrejaRepository.getIgrejasByOrganista(it.id).collect { igrejas ->
                        _uiState.update { state -> state.copy(igrejas = igrejas) }
                    }
                }
            }
        }
    }

    fun selecionarIgreja(igreja: Igreja?) {
        _uiState.update { it.copy(
            igrejaSelecionada = igreja,
            nomeIgrejaManual = igreja?.nome ?: it.nomeIgrejaManual,
            hora = if (igreja != null) {
                try {
                    val partes = igreja.horarioPadrao.split(":")
                    LocalTime.of(partes[0].toInt(), partes[1].toInt())
                } catch (_: Exception) { it.hora }
            } else it.hora
        )}
    }

    fun atualizarNomeIgreja(nome: String) {
        _uiState.update { it.copy(nomeIgrejaManual = nome, igrejaSelecionada = null) }
    }

    fun atualizarData(data: LocalDate) {
        _uiState.update { it.copy(data = data) }
    }

    fun atualizarHora(hora: LocalTime) {
        _uiState.update { it.copy(hora = hora) }
    }

    fun salvar() {
        viewModelScope.launch {
            val state = _uiState.value
            val nomeIgreja = state.igrejaSelecionada?.nome ?: state.nomeIgrejaManual

            if (nomeIgreja.isBlank()) {
                _uiState.update { it.copy(erro = "Informe o nome da igreja.") }
                return@launch
            }

            _uiState.update { it.copy(salvando = true, erro = null) }

            try {
                val organista = organistaRepository.getOrganistaPrincipal().first()
                    ?: run {
                        _uiState.update { it.copy(salvando = false, erro = "Configure seu perfil primeiro.") }
                        return@launch
                    }

                val dataHora = LocalDateTime.of(state.data, state.hora)
                val dataHoraMillis = dataHora.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val titulo = "Escala - $nomeIgreja"

                // Escala manual
                val escalaId = escalaRepository.saveEscala(
                    Escala(
                        organistaId = organista.id,
                        nomeArquivo = "Manual",
                        caminhoArquivo = "",
                        textoExtraido = "",
                        mes = state.data.monthValue,
                        ano = state.data.year
                    )
                )

                var googleEventId = ""
                if (organista.googleAccountEmail.isNotBlank()) {
                    try {
                        googleEventId = calendarUtils.criarEvento(
                            accountEmail = organista.googleAccountEmail,
                            titulo = titulo,
                            nomeIgreja = nomeIgreja,
                            dataHora = dataHora
                        )
                    } catch (_: Exception) { }
                }

                val eventoId = eventoRepository.saveEvento(
                    Evento(
                        escalaId = escalaId,
                        organistaId = organista.id,
                        titulo = titulo,
                        nomeIgreja = nomeIgreja,
                        dataHora = dataHoraMillis,
                        googleCalendarEventId = googleEventId
                    )
                )

                eventoRepository.getEventoById(eventoId)?.let {
                    notificationUtils.agendarNotificacoes(it)
                }

                _uiState.update { it.copy(salvando = false, sucesso = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(salvando = false, erro = "Erro: ${e.message}") }
            }
        }
    }
}
