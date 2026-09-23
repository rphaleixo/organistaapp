package com.organistaapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "eventos",
    foreignKeys = [
        ForeignKey(
            entity = Escala::class,
            parentColumns = ["id"],
            childColumns = ["escalaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("escalaId"), Index("dataHora")]
)
data class Evento(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val escalaId: Long,
    val organistaId: Long,
    val titulo: String,
    val nomeIgreja: String,
    val dataHora: Long,
    val duracao: Int = 120,
    val googleCalendarEventId: String = "",
    val notificacao72hAgendada: Boolean = false,
    val notificacaoDiaAgendada: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
