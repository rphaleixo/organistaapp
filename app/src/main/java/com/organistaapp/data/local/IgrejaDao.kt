package com.organistaapp.data.local

import androidx.room.*
import com.organistaapp.data.model.Igreja
import kotlinx.coroutines.flow.Flow

@Dao
interface IgrejaDao {
    @Query("SELECT * FROM igrejas WHERE organistaId = :organistaId ORDER BY nome ASC")
    fun getIgrejasByOrganista(organistaId: Long): Flow<List<Igreja>>

    @Query("SELECT * FROM igrejas WHERE id = :id")
    suspend fun getIgrejaById(id: Long): Igreja?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIgreja(igreja: Igreja): Long

    @Update
    suspend fun updateIgreja(igreja: Igreja)

    @Delete
    suspend fun deleteIgreja(igreja: Igreja)
}
