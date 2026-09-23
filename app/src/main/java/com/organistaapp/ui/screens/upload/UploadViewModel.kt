package com.organistaapp.ui.screens.upload

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.organistaapp.data.model.Escala
import com.organistaapp.data.model.Evento
import com.organistaapp.data.repository.EscalaRepository
import com.organistaapp.data.repository.EventoRepository
import com.organistaapp.data.repository.OrganistaRepository
import com.organistaapp.utils.GoogleCalendarUtils
import com.organistaapp.utils.NotificationUtils
import com.organistaapp.utils.OcrUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class TextoExtraido(val texto: String, val eventosEncontrados: Int) : UploadState()
    data class Sucesso(val eventosAdicionados: Int) : UploadState()
    data class Erro(val mensagem: String) : UploadState()
}

data class UploadUiState(
    val uploadState: UploadState = UploadState.Idle,
    val arquivoSelecionado: String? = null,
    val textoPreview: String = ""
)

@HiltViewModel
class UploadViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val organistaRepository: OrganistaRepository,
    private val escalaRepository: EscalaRepository,
    private val eventoRepository: EventoRepository,
    private val ocrUtils: OcrUtils,
    private val calendarUtils: GoogleCalendarUtils,
    private val notificationUtils: NotificationUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    fun processarArquivo(uri: Uri, nomeArquivo: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(
                uploadState = UploadState.Loading,
                arquivoSelecionado = nomeArquivo
            )}

            try {
                val organista = organistaRepository.getOrganistaPrincipal().first()
                    ?: run {
                        _uiState.update { it.copy(
                            uploadState = UploadState.Erro("Configure seu perfil antes de enviar a escala.")
                        )}
                        return@launch
                    }

                val texto = ocrUtils.extrairTextoDeImagem(uri)
                val eventos = ocrUtils.extrairEventosDaEscala(texto, organista.nome)

                _uiState.update { it.copy(
                    uploadState = UploadState.TextoExtraido(texto, eventos.size),
                    textoPreview = texto.take(500)
                )}

                if (eventos.isEmpty()) {
                    _uiState.update { it.copy(
                        uploadState = UploadState.Erro(
                            "Não foram encontrados eventos com seu nome na escala. " +
                            "Verifique se o nome no perfil está correto."
                        )
                    )}
                    return@launch
                }

                // Salva a escala no banco
                val agora = System.currentTimeMillis()
                val primeirEvento = eventos.first()
                val escalaId = escalaRepository.saveEscala(
                    Escala(
                        organistaId = organista.id,
                        nomeArquivo = nomeArquivo,
                        caminhoArquivo = uri.toString(),
                        textoExtraido = texto,
                        mes = primeirEvento.data.monthValue,
                        ano = primeirEvento.data.year,
                        uploadedAt = agora
                    )
                )

                // Cria eventos no banco e no Google Calendar
                var eventosAdicionados = 0
                for (eventoExtraido in eventos) {
                    val dataHora = LocalDateTime.of(eventoExtraido.data, eventoExtraido.hora)
                    val dataHoraMillis = dataHora.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                    val titulo = "Escala - ${eventoExtraido.nomeIgreja}"
                    var googleEventId = ""

                    if (organista.googleAccountEmail.isNotBlank()) {
                        try {
                            googleEventId = calendarUtils.criarEvento(
                                accountEmail = organista.googleAccountEmail,
                                titulo = titulo,
                                nomeIgreja = eventoExtraido.nomeIgreja,
                                dataHora = dataHora
                            )
                        } catch (_: Exception) { }
                    }

                    val eventoId = eventoRepository.saveEvento(
                        Evento(
                            escalaId = escalaId,
                            organistaId = organista.id,
                            titulo = titulo,
                            nomeIgreja = eventoExtraido.nomeIgreja,
                            dataHora = dataHoraMillis,
                            googleCalendarEventId = googleEventId
                        )
                    )

                    // Agenda notificações
                    val eventoSalvo = eventoRepository.getEventoById(eventoId)
                    eventoSalvo?.let { notificationUtils.agendarNotificacoes(it) }

                    eventosAdicionados++
                }

                _uiState.update { it.copy(
                    uploadState = UploadState.Sucesso(eventosAdicionados)
                )}

            } catch (e: Exception) {
                _uiState.update { it.copy(
                    uploadState = UploadState.Erro("Erro ao processar arquivo: ${e.message}")
                )}
            }
        }
    }

    fun resetar() {
        _uiState.update { UploadUiState() }
    }
}
