package com.organistaapp.utils

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.EventReminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleCalendarUtils @Inject constructor(
    private val context: Context
) {
    private fun getCalendarService(accountEmail: String): Calendar {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(CalendarScopes.CALENDAR)
        ).apply {
            selectedAccountName = accountEmail
        }

        return Calendar.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("OrganistaApp")
            .build()
    }

    suspend fun criarEvento(
        accountEmail: String,
        titulo: String,
        nomeIgreja: String,
        dataHora: LocalDateTime,
        duracaoMinutos: Int = 120
    ): String = withContext(Dispatchers.IO) {
        val service = getCalendarService(accountEmail)

        val zone = ZoneId.systemDefault()
        val startMillis = dataHora.atZone(zone).toInstant().toEpochMilli()
        val endMillis = dataHora.plusMinutes(duracaoMinutos.toLong()).atZone(zone).toInstant().toEpochMilli()

        val event = Event().apply {
            summary = titulo
            location = nomeIgreja
            description = "Escala de organista - $nomeIgreja"

            start = EventDateTime().apply {
                dateTime = DateTime(startMillis)
                timeZone = zone.id
            }
            end = EventDateTime().apply {
                dateTime = DateTime(endMillis)
                timeZone = zone.id
            }

            // Lembrete padrão do Google Calendar (não substitui as notificações locais)
            reminders = Event.Reminders().apply {
                useDefault = false
                overrides = listOf(
                    EventReminder().apply {
                        method = "popup"
                        minutes = 60
                    }
                )
            }
        }

        val createdEvent = service.events()
            .insert("primary", event)
            .execute()

        createdEvent.id ?: ""
    }

    suspend fun deletarEvento(accountEmail: String, eventId: String) = withContext(Dispatchers.IO) {
        if (eventId.isBlank()) return@withContext
        try {
            val service = getCalendarService(accountEmail)
            service.events().delete("primary", eventId).execute()
        } catch (_: Exception) { }
    }
}
