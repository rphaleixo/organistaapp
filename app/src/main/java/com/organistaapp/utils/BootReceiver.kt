package com.organistaapp.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.organistaapp.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        CoroutineScope(Dispatchers.IO).launch {
            val db = getDatabase(context)
            val notificationUtils = NotificationUtils(context)
            val agora = System.currentTimeMillis()

            val eventos = db.eventoDao().getProximosEventos(agora).first()
            eventos.forEach { evento ->
                notificationUtils.agendarNotificacoes(evento)
            }
        }
    }

    private fun getDatabase(context: Context): AppDatabase {
        return androidx.room.Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "organista_db"
        ).build()
    }
}
