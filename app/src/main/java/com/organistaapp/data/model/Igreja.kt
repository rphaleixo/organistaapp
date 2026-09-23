package com.organistaapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "igrejas",
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
data class Igreja(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val organistaId: Long,
    val nome: String,
    val endereco: String = "",
    val horarioPadrao: String = "08:00",
    val cor: String = "#6B3FA0",
    val createdAt: Long = System.currentTimeMillis()
)
