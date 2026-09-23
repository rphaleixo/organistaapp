package com.organistaapp.data.local

import androidx.room.*
import com.organistaapp.data.model.Escala
import kotlinx.coroutines.flow.Flow

@Dao
interface EscalaDao {
    @Query("SELECT * FROM escalas WHERE organistaId = :organistaId ORDER BY ano DESC, mes DESC")
    fun getEscalasByOrganista(organistaId: Long): Flow<List<Escala>>

    @Query("SELECT * FROM escalas WHERE id = :id")
    suspend fun getEscalaById(id: Long): Escala?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEscala(escala: Escala): Long

    @Delete
    suspend fun deleteEscala(escala: Escala)
}
