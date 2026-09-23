package com.organistaapp.utils

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleCalendarUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Retorna o ID do primeiro calendário editável encontrado para a conta,
    // ou o primeiro calendário disponível como fallback.
    private fun encontrarCalendarioId(accountEmail: String): Long? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_NAME
        )

        // Tenta encontrar calendário primário da conta
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.ACCOUNT_NAME} = ? AND ${CalendarContract.Calendars.IS_PRIMARY} = 1",
            arrayOf(accountEmail),
            null
        )?.use { if (it.moveToFirst()) return it.getLong(0) }

        // Fallback: qualquer calendário da conta
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.ACCOUNT_NAME} = ?",
            arrayOf(accountEmail),
            null
        )?.use { if (it.moveToFirst()) return it.getLong(0) }

        // Fallback final: primeiro calendário disponível
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null, null, null
        )?.use { if (it.moveToFirst()) return it.getLong(0) }

        return null
    }

    suspend fun criarEvento(
        accountEmail: String,
        titulo: String,
        nomeIgreja: String,
        dataHora: LocalDateTime,
        duracaoMinutos: Int = 120
    ): String = withContext(Dispatchers.IO) {
        try {
            val zone = ZoneId.systemDefault()
            val startMillis = dataHora.atZone(zone).toInstant().toEpochMilli()
            val endMillis = dataHora.plusMinutes(duracaoMinutos.toLong()).atZone(zone).toInstant().toEpochMilli()

            val calendarId = encontrarCalendarioId(accountEmail) ?: return@withContext ""

            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.TITLE, titulo)
                put(CalendarContract.Events.DESCRIPTION, "Escala de organista — $nomeIgreja")
                put(CalendarContract.Events.EVENT_LOCATION, nomeIgreja)
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.EVENT_TIMEZONE, zone.id)
            }

            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            uri?.lastPathSegment ?: ""
        } catch (_: Exception) { "" }
    }

    suspend fun deletarEvento(accountEmail: String, eventId: String) = withContext(Dispatchers.IO) {
        if (eventId.isBlank()) return@withContext
        try {
            val id = eventId.toLongOrNull() ?: return@withContext
            val deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id)
            context.contentResolver.delete(deleteUri, null, null)
        } catch (_: Exception) { }
    }
}
