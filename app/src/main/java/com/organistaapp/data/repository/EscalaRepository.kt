package com.organistaapp.data.repository

import com.organistaapp.data.local.EscalaDao
import com.organistaapp.data.model.Escala
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EscalaRepository @Inject constructor(
    private val dao: EscalaDao
) {
    fun getEscalasByOrganista(organistaId: Long): Flow<List<Escala>> =
        dao.getEscalasByOrganista(organistaId)

    suspend fun getEscalaById(id: Long): Escala? = dao.getEscalaById(id)

    suspend fun saveEscala(escala: Escala): Long = dao.insertEscala(escala)

    suspend fun deleteEscala(escala: Escala) = dao.deleteEscala(escala)
}
