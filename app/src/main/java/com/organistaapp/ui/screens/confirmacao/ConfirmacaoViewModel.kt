package com.organistaapp.ui.screens.confirmacao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.organistaapp.data.model.Escala
import com.organistaapp.data.model.Evento
import com.organistaapp.data.repository.EscalaRepository
import com.organistaapp.data.repository.EventoRepository
import com.organistaapp.data.repository.OrganistaRepository
import com.organistaapp.utils.EventoExtraido
import com.organistaapp.utils.GoogleCalendarUtils
import com.organistaapp.utils.NotificationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

data class EventoEditavel(
    val id: String = java.util.UUID.randomUUID().toString(),
    val nomeIgreja: String,
    val data: java.time.LocalDate,
    val hora: LocalTime,
    val incluir: Boolean = true
)

data class ConfirmacaoUiState(
    val eventos: List<EventoEditavel> = emptyList(),
    val salvando: Boolean = false,
    val sucesso: Boolean = false,
    val erro: String? = null,
    val textoEscala: String = "",
    val nomeArquivo: String = ""
)

@HiltViewModel
class ConfirmacaoViewModel @Inject constructor(
    private val organistaRepository: OrganistaRepository,
    private val escalaRepository: EscalaRepository,
    private val eventoRepository: EventoRepository,
    private val calendarUtils: GoogleCalendarUtils,
    private val notificationUtils: NotificationUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfirmacaoUiState())
    val uiState: StateFlow<ConfirmacaoUiState> = _uiState.asStateFlow()

    fun carregarEventos(
        eventosExtraidos: List<EventoExtraido>,
        textoEscala: String,
        nomeArquivo: String,
        caminhoArquivo: String
    ) {
        _nomeArquivo = nomeArquivo
        _caminhoArquivo = caminhoArquivo
        _textoEscala = textoEscala

        val editaveis = eventosExtraidos.map { ev ->
            EventoEditavel(
                nomeIgreja = ev.nomeIgreja,
                data = ev.data,
                hora = ev.hora
            )
        }
        _uiState.update { it.copy(
            eventos = editaveis,
            textoEscala = textoEscala,
            nomeArquivo = nomeArquivo
        )}
    }

    private var _nomeArquivo = ""
    private var _caminhoArquivo = ""
    private var _textoEscala = ""

    fun toggleEvento(id: String) {
        _uiState.update { state ->
            state.copy(
                eventos = state.eventos.map {
                    if (it.id == id) it.copy(incluir = !it.incluir) else it
                }
            )
        }
    }

    fun atualizarHora(id: String, hora: LocalTime) {
        _uiState.update { state ->
            state.copy(
                eventos = state.eventos.map {
                    if (it.id == id) it.copy(hora = hora) else it
                }
            )
        }
    }

    fun atualizarIgreja(id: String, nome: String) {
        _uiState.update { state ->
            state.copy(
                eventos = state.eventos.map {
                    if (it.id == id) it.copy(nomeIgreja = nome) else it
                }
            )
        }
    }

    fun confirmarESalvar() {
        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, erro = null) }

            try {
                val organista = organistaRepository.getOrganistaPrincipal().first()
                    ?: run {
                        _uiState.update { it.copy(salvando = false, erro = "Perfil não encontrado.") }
                        return@launch
                    }

                val selecionados = _uiState.value.eventos.filter { it.incluir }
                if (selecionados.isEmpty()) {
                    _uiState.update { it.copy(salvando = false, erro = "Selecione ao menos um evento.") }
                    return@launch
                }

                val primeiroEvento = selecionados.first()
                val escalaId = escalaRepository.saveEscala(
                    Escala(
                        organistaId = organista.id,
                        nomeArquivo = _nomeArquivo,
                        caminhoArquivo = _caminhoArquivo,
                        textoExtraido = _textoEscala,
                        mes = primeiroEvento.data.monthValue,
                        ano = primeiroEvento.data.year
                    )
                )

                for (ev in selecionados) {
                    val dataHora = LocalDateTime.of(ev.data, ev.hora)
                    val dataHoraMillis = dataHora.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val titulo = "Escala - ${ev.nomeIgreja}"

                    var googleEventId = ""
                    if (organista.googleAccountEmail.isNotBlank()) {
                        try {
                            googleEventId = calendarUtils.criarEvento(
                                accountEmail = organista.googleAccountEmail,
                                titulo = titulo,
                                nomeIgreja = ev.nomeIgreja,
                                dataHora = dataHora
                            )
                        } catch (_: Exception) { }
                    }

                    val eventoId = eventoRepository.saveEvento(
                        Evento(
                            escalaId = escalaId,
                            organistaId = organista.id,
                            titulo = titulo,
                            nomeIgreja = ev.nomeIgreja,
                            dataHora = dataHoraMillis,
                            googleCalendarEventId = googleEventId
                        )
                    )
                    eventoRepository.getEventoById(eventoId)?.let {
                        notificationUtils.agendarNotificacoes(it)
                    }
                }

                _uiState.update { it.copy(salvando = false, sucesso = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(salvando = false, erro = "Erro ao salvar: ${e.message}") }
            }
        }
    }
}
