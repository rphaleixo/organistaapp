package com.organistaapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.*
import com.organistaapp.MainActivity
import com.organistaapp.data.local.AppDatabase
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class EscalaWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val evento = buscarProximoEvento(context)

        provideContent {
            WidgetContent(evento)
        }
    }

    private suspend fun buscarProximoEvento(context: Context): ProximoEventoInfo? {
        return try {
            val db = AppDatabase_Helper.getDatabase(context)
            val agora = System.currentTimeMillis()
            val eventos = db.eventoDao().getProximosEventos(agora).first()
            val proximo = eventos.firstOrNull() ?: return null

            val dataHora = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(proximo.dataHora),
                ZoneId.systemDefault()
            )
            ProximoEventoInfo(
                titulo = proximo.titulo,
                nomeIgreja = proximo.nomeIgreja,
                dataHora = dataHora
            )
        } catch (_: Exception) { null }
    }
}

data class ProximoEventoInfo(
    val titulo: String,
    val nomeIgreja: String,
    val dataHora: LocalDateTime
)

@Composable
private fun WidgetContent(evento: ProximoEventoInfo?) {
    val roxo = androidx.glance.unit.ColorProvider(Color(0xFF6B3FA0))
    val branco = androidx.glance.unit.ColorProvider(Color.White)
    val brancoAlpha = androidx.glance.unit.ColorProvider(Color.White.copy(alpha = 0.8f))
    val dourado = androidx.glance.unit.ColorProvider(Color(0xFFD4A843))

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF6B3FA0))
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "Próxima Escala",
                style = TextStyle(
                    color = dourado,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))

            if (evento != null) {
                val formatter = DateTimeFormatter.ofPattern("dd/MM · HH:mm")
                val diaSemana = evento.dataHora.dayOfWeek
                    .getDisplayName(java.time.format.TextStyle.SHORT, Locale("pt", "BR"))
                    .replaceFirstChar { it.uppercase() }

                Text(
                    text = evento.nomeIgreja,
                    style = TextStyle(
                        color = branco,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = "$diaSemana · ${evento.dataHora.format(formatter)}",
                    style = TextStyle(
                        color = brancoAlpha,
                        fontSize = 12.sp
                    )
                )
            } else {
                Text(
                    text = "Sem eventos próximos",
                    style = TextStyle(
                        color = brancoAlpha,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = "Envie uma escala",
                    style = TextStyle(
                        color = dourado,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

class EscalaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = EscalaWidget()
}

private object AppDatabase_Helper {
    @Volatile private var instance: AppDatabase? = null
    fun getDatabase(context: Context): AppDatabase = instance ?: synchronized(this) {
        instance ?: androidx.room.Room.databaseBuilder(
            context.applicationContext, AppDatabase::class.java, "organista_db"
        ).build().also { instance = it }
    }
}
