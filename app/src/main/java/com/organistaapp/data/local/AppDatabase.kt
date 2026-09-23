package com.organistaapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.organistaapp.data.model.Escala
import com.organistaapp.data.model.Evento
import com.organistaapp.data.model.Igreja
import com.organistaapp.data.model.Organista

@Database(
    entities = [Organista::class, Escala::class, Evento::class, Igreja::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun organistaDao(): OrganistaDao
    abstract fun escalaDao(): EscalaDao
    abstract fun eventoDao(): EventoDao
    abstract fun igrejaDao(): IgrejaDao
}
