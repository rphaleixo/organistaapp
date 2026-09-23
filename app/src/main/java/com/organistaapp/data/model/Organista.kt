package com.organistaapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "organistas")
data class Organista(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val email: String = "",
    val googleAccountEmail: String = "",
    val igrejasPrincipais: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
