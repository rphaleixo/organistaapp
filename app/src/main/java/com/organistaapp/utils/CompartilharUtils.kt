package com.organistaapp.utils

import android.content.Context
import android.content.Intent
import com.organistaapp.data.model.Evento
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompartilharUtils @Inject constructor() {

    fun compartilharEscala(context: Context, eventos: List<Evento>, nomeOrganista: String) {
        if (eventos.isEmpty()) return

        val sb = StringBuilder()
        sb.appendLine("📅 *Escala de $nomeOrganista*")
        sb.appendLine()

        // Agrupa por mês
        val porMes = eventos.groupBy { evento ->
            val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(evento.dataHora), ZoneId.systemDefault())
            "${dt.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR")).replaceFirstChar { it.uppercase() }} ${dt.year}"
        }

        for ((mes, eventosDoMes) in porMes) {
            sb.appendLine("*$mes*")
            for (evento in eventosDoMes.sortedBy { it.dataHora }) {
                val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(evento.dataHora), ZoneId.systemDefault())
                val diaSemana = dt.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pt", "BR"))
                    .replaceFirstChar { it.uppercase() }
                val hora = dt.format(DateTimeFormatter.ofPattern("HH:mm"))
                sb.appendLine("• $diaSemana ${dt.dayOfMonth} - ${evento.nomeIgreja} às $hora")
            }
            sb.appendLine()
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString().trim())
            putExtra(Intent.EXTRA_SUBJECT, "Escala de $nomeOrganista")
        }

        context.startActivity(Intent.createChooser(intent, "Compartilhar escala via..."))
    }

    fun compartilharMes(
        context: Context,
        eventos: List<Evento>,
        nomeOrganista: String,
        mes: String
    ) {
        val filtrados = eventos.filter { evento ->
            val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(evento.dataHora), ZoneId.systemDefault())
            "${dt.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR")).replaceFirstChar { it.uppercase() }} ${dt.year}" == mes
        }
        compartilharEscala(context, filtrados, nomeOrganista)
    }
}
