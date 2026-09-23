package com.organistaapp.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.organistaapp.data.model.Evento
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationUtils @Inject constructor(
    private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "organista_escalas"
        const val CHANNEL_NAME = "Escalas de Organista"
        const val EXTRA_EVENTO_ID = "evento_id"
        const val EXTRA_TIPO_NOTIFICACAO = "tipo_notificacao"
        const val TIPO_72H = "72h"
        const val TIPO_DIA = "dia"
    }

    fun criarCanalNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Lembretes de escala de organista"
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun agendarNotificacoes(evento: Evento) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val dataHora = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(evento.dataHora),
            ZoneId.systemDefault()
        )

        // Notificação 72h antes
        val tempo72h = dataHora.minusHours(72)
        val millis72h = tempo72h.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (millis72h > System.currentTimeMillis()) {
            agendarAlarme(alarmManager, evento.id, TIPO_72H, millis72h)
        }

        // Notificação no dia às 9h
        val tempoDia = dataHora.toLocalDate().atTime(9, 0)
        val millisDia = tempoDia.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (millisDia > System.currentTimeMillis()) {
            agendarAlarme(alarmManager, evento.id, TIPO_DIA, millisDia)
        }
    }

    fun cancelarNotificacoes(eventoId: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        cancelarAlarme(alarmManager, eventoId, TIPO_72H)
        cancelarAlarme(alarmManager, eventoId, TIPO_DIA)
    }

    private fun agendarAlarme(
        alarmManager: AlarmManager,
        eventoId: Long,
        tipo: String,
        triggerMillis: Long
    ) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_EVENTO_ID, eventoId)
            putExtra(EXTRA_TIPO_NOTIFICACAO, tipo)
        }
        val requestCode = "${eventoId}_${tipo}".hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    private fun cancelarAlarme(alarmManager: AlarmManager, eventoId: Long, tipo: String) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val requestCode = "${eventoId}_${tipo}".hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }
}
