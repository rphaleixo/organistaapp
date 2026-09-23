package com.organistaapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class EventoExtraido(
    val nomeIgreja: String,
    val data: LocalDate,
    val hora: LocalTime,
    val descricao: String
)

@Singleton
class OcrUtils @Inject constructor(
    private val context: Context
) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extrairTextoDeImagem(uri: Uri): String {
        val image = InputImage.fromFilePath(context, uri)
        return reconhecerTexto(image)
    }

    suspend fun extrairTextoDeImagem(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        return reconhecerTexto(image)
    }

    private suspend fun reconhecerTexto(image: InputImage): String =
        suspendCancellableCoroutine { cont ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    cont.resume(visionText.text)
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }

    fun extrairEventosDaEscala(texto: String, nomeOrganista: String): List<EventoExtraido> {
        val eventos = mutableListOf<EventoExtraido>()
        val linhas = texto.lines()

        var igrejaAtual = ""
        var mesAtual = 0
        var anoAtual = LocalDate.now().year

        for (linha in linhas) {
            val linhaLimpa = linha.trim()
            if (linhaLimpa.isBlank()) continue

            // Detecta nome da igreja
            val nomeIgreja = extrairNomeIgreja(linhaLimpa)
            if (nomeIgreja != null) {
                igrejaAtual = nomeIgreja
                continue
            }

            // Detecta mês/ano no cabeçalho
            val mesAno = extrairMesAno(linhaLimpa)
            if (mesAno != null) {
                mesAtual = mesAno.first
                anoAtual = mesAno.second
                continue
            }

            // Verifica se a linha menciona o organista
            if (!contemNome(linhaLimpa, nomeOrganista)) continue

            // Extrai dia e hora da linha
            val dia = extrairDia(linhaLimpa)
            val hora = extrairHora(linhaLimpa)

            if (dia != null && mesAtual > 0) {
                try {
                    val data = LocalDate.of(anoAtual, mesAtual, dia)
                    val horario = hora ?: LocalTime.of(8, 0)
                    eventos.add(
                        EventoExtraido(
                            nomeIgreja = igrejaAtual.ifBlank { "Igreja" },
                            data = data,
                            hora = horario,
                            descricao = linhaLimpa
                        )
                    )
                } catch (_: Exception) { }
            }
        }

        return eventos
    }

    private fun extrairNomeIgreja(linha: String): String? {
        val padroes = listOf(
            Regex("(?i)(paróquia|parish|ig\\.|igreja|catedral|santuário|capela)\\s+.+", RegexOption.IGNORE_CASE),
            Regex("(?i)^(nossa\\s+senhora|são\\s+|santa\\s+|santo\\s+|sagrado\\s+).+")
        )
        return padroes.firstNotNullOfOrNull { regex ->
            regex.find(linha)?.value?.trim()
        }
    }

    private fun extrairMesAno(linha: String): Pair<Int, Int>? {
        val meses = mapOf(
            "janeiro" to 1, "fevereiro" to 2, "março" to 3, "abril" to 4,
            "maio" to 5, "junho" to 6, "julho" to 7, "agosto" to 8,
            "setembro" to 9, "outubro" to 10, "novembro" to 11, "dezembro" to 12,
            "jan" to 1, "fev" to 2, "mar" to 3, "abr" to 4,
            "mai" to 5, "jun" to 6, "jul" to 7, "ago" to 8,
            "set" to 9, "out" to 10, "nov" to 11, "dez" to 12
        )

        val regexMesAno = Regex("(\\w+)\\s*/\\s*(\\d{4})", RegexOption.IGNORE_CASE)
        val matchMesAno = regexMesAno.find(linha)
        if (matchMesAno != null) {
            val nomeMes = matchMesAno.groupValues[1].lowercase()
            val ano = matchMesAno.groupValues[2].toIntOrNull() ?: return null
            val mes = meses[nomeMes] ?: return null
            return Pair(mes, ano)
        }

        val regexNumerico = Regex("(\\d{1,2})/(\\d{4})")
        val matchNumerico = regexNumerico.find(linha)
        if (matchNumerico != null) {
            val mes = matchNumerico.groupValues[1].toIntOrNull() ?: return null
            val ano = matchNumerico.groupValues[2].toIntOrNull() ?: return null
            if (mes in 1..12) return Pair(mes, ano)
        }

        // Mês sozinho em texto
        for ((nomeMes, numMes) in meses) {
            if (linha.lowercase().contains(nomeMes)) {
                val regexAno = Regex("\\b(20\\d{2})\\b")
                val ano = regexAno.find(linha)?.value?.toIntOrNull() ?: LocalDate.now().year
                return Pair(numMes, ano)
            }
        }

        return null
    }

    private fun extrairDia(linha: String): Int? {
        // Padrões como "dia 15", "15/", "15 -", "15."
        val padroes = listOf(
            Regex("\\bdia\\s+(\\d{1,2})\\b", RegexOption.IGNORE_CASE),
            Regex("^(\\d{1,2})[/\\-\\.]"),
            Regex("\\b(\\d{1,2})\\s*[-–]"),
            Regex("^(\\d{1,2})\\s")
        )
        for (regex in padroes) {
            val match = regex.find(linha)
            val dia = match?.groupValues?.get(1)?.toIntOrNull()
            if (dia != null && dia in 1..31) return dia
        }
        return null
    }

    private fun extrairHora(linha: String): LocalTime? {
        val regexHora = Regex("(\\d{1,2})[:h](\\d{2})?(?:\\s*(?:h|hrs|horas))?", RegexOption.IGNORE_CASE)
        val match = regexHora.find(linha) ?: return null
        val hora = match.groupValues[1].toIntOrNull() ?: return null
        val minuto = match.groupValues[2].toIntOrNull() ?: 0
        return if (hora in 0..23 && minuto in 0..59) LocalTime.of(hora, minuto) else null
    }

    private fun contemNome(linha: String, nome: String): Boolean {
        if (nome.isBlank()) return true
        val nomeParts = nome.lowercase().split(" ")
        val linhaLower = linha.lowercase()
        return nomeParts.any { part -> part.length > 2 && linhaLower.contains(part) }
    }
}
