package com.organistaapp.data.local

import androidx.room.*
import com.organistaapp.data.model.Organista
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganistaDao {
    @Query("SELECT * FROM organistas ORDER BY nome ASC")
    fun getAllOrganistas(): Flow<List<Organista>>

    @Query("SELECT * FROM organistas WHERE id = :id")
    suspend fun getOrganistaById(id: Long): Organista?

    @Query("SELECT * FROM organistas LIMIT 1")
    fun getOrganistaPrincipal(): Flow<Organista?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganista(organista: Organista): Long

    @Update
    suspend fun updateOrganista(organista: Organista)

    @Delete
    suspend fun deleteOrganista(organista: Organista)
}
