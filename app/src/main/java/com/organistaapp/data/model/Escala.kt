package com.organistaapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "escalas",
    foreignKeys = [
        ForeignKey(
            entity = Organista::class,
            parentColumns = ["id"],
            childColumns = ["organistaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("organistaId")]
)
data class Escala(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val organistaId: Long,
    val nomeArquivo: String,
    val caminhoArquivo: String,
    val textoExtraido: String = "",
    val mes: Int,
    val ano: Int,
    val uploadedAt: Long = System.currentTimeMillis()
)
