package com.organistaapp.data.local

import androidx.room.*
import com.organistaapp.data.model.Evento
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDao {
    @Query("SELECT * FROM eventos WHERE organistaId = :organistaId ORDER BY dataHora ASC")
    fun getEventosByOrganista(organistaId: Long): Flow<List<Evento>>

    @Query("""
        SELECT * FROM eventos
        WHERE organistaId = :organistaId
        AND dataHora >= :inicioMes
        AND dataHora < :fimMes
        ORDER BY dataHora ASC
    """)
    fun getEventosByMes(organistaId: Long, inicioMes: Long, fimMes: Long): Flow<List<Evento>>

    @Query("SELECT * FROM eventos WHERE id = :id")
    suspend fun getEventoById(id: Long): Evento?

    @Query("SELECT * FROM eventos WHERE dataHora > :agora ORDER BY dataHora ASC")
    fun getProximosEventos(agora: Long): Flow<List<Evento>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvento(evento: Evento): Long

    @Update
    suspend fun updateEvento(evento: Evento)

    @Delete
    suspend fun deleteEvento(evento: Evento)

    @Query("DELETE FROM eventos WHERE escalaId = :escalaId")
    suspend fun deleteEventosByEscala(escalaId: Long)
}
