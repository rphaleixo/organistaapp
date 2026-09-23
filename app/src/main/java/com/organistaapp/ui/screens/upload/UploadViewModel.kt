package com.organistaapp.ui.screens.upload

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.organistaapp.data.repository.OrganistaRepository
import com.organistaapp.utils.EventoExtraido
import com.organistaapp.utils.OcrUtils
import com.organistaapp.utils.PdfUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class ProntoParaConfirmar(
        val eventosExtraidos: List<EventoExtraido>,
        val textoEscala: String,
        val nomeArquivo: String,
        val caminhoArquivo: String
    ) : UploadState()
    data class Erro(val mensagem: String) : UploadState()
}

data class UploadUiState(
    val uploadState: UploadState = UploadState.Idle,
    val arquivoSelecionado: String? = null
)

@HiltViewModel
class UploadViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val organistaRepository: OrganistaRepository,
    private val ocrUtils: OcrUtils,
    private val pdfUtils: PdfUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    fun processarArquivo(uri: Uri, nomeArquivo: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(uploadState = UploadState.Loading, arquivoSelecionado = nomeArquivo) }

            try {
                val organista = organistaRepository.getOrganistaPrincipal().first()
                    ?: run {
                        _uiState.update { it.copy(uploadState = UploadState.Erro("Configure seu perfil antes de enviar a escala.")) }
                        return@launch
                    }

                val texto = if (pdfUtils.isPdf(uri)) {
                    pdfUtils.extrairTextoDePdf(uri)
                } else {
                    ocrUtils.extrairTextoDeImagem(uri)
                }

                if (texto.isBlank()) {
                    _uiState.update { it.copy(uploadState = UploadState.Erro("Não foi possível extrair texto do arquivo. Tente uma imagem mais nítida.")) }
                    return@launch
                }

                val eventos = ocrUtils.extrairEventosDaEscala(texto, organista.nome)

                if (eventos.isEmpty()) {
                    _uiState.update { it.copy(
                        uploadState = UploadState.Erro(
                            "Nenhum evento encontrado com o nome \"${organista.nome}\" na escala.\n\n" +
                            "Verifique se o nome no perfil está idêntico ao da escala."
                        )
                    )}
                    return@launch
                }

                _uiState.update { it.copy(
                    uploadState = UploadState.ProntoParaConfirmar(
                        eventosExtraidos = eventos,
                        textoEscala = texto,
                        nomeArquivo = nomeArquivo,
                        caminhoArquivo = uri.toString()
                    )
                )}

            } catch (e: Exception) {
                _uiState.update { it.copy(uploadState = UploadState.Erro("Erro ao processar arquivo: ${e.message}")) }
            }
        }
    }

    fun resetar() {
        _uiState.update { UploadUiState() }
    }
}
