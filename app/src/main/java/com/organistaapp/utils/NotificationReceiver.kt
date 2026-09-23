package com.organistaapp.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.organistaapp.MainActivity
import com.organistaapp.R
import com.organistaapp.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val eventoId = intent.getLongExtra(NotificationUtils.EXTRA_EVENTO_ID, -1L)
        val tipoNotificacao = intent.getStringExtra(NotificationUtils.EXTRA_TIPO_NOTIFICACAO) ?: return

        if (eventoId == -1L) return

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase_Impl.getDatabase(context)
            val evento = db.eventoDao().getEventoById(eventoId) ?: return@launch

            val dataHora = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(evento.dataHora),
                ZoneId.systemDefault()
            )
            val formatter = DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm")
            val dataFormatada = dataHora.format(formatter)

            val titulo: String
            val mensagem: String

            when (tipoNotificacao) {
                NotificationUtils.TIPO_72H -> {
                    titulo = "Lembrete de Escala - 3 dias"
                    mensagem = "Você tem escala em ${evento.nomeIgreja} no dia $dataFormatada"
                }
                NotificationUtils.TIPO_DIA -> {
                    titulo = "Escala Hoje!"
                    mensagem = "Você tem escala em ${evento.nomeIgreja} às ${dataHora.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                }
                else -> return@launch
            }

            mostrarNotificacao(context, eventoId.toInt(), titulo, mensagem)
        }
    }

    private fun mostrarNotificacao(context: Context, id: Int, titulo: String, mensagem: String) {
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, id, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationUtils.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensagem))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(id, notification)
    }
}

// Extension para obter a instância do banco fora do Hilt
private object AppDatabase_Impl {
    @Volatile private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: androidx.room.Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "organista_db"
            ).build().also { instance = it }
        }
    }
}
