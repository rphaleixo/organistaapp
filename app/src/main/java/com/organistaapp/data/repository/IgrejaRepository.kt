package com.organistaapp.data.repository

import com.organistaapp.data.local.IgrejaDao
import com.organistaapp.data.model.Igreja
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IgrejaRepository @Inject constructor(
    private val dao: IgrejaDao
) {
    fun getIgrejasAtivas(organistaId: Long): Flow<List<Igreja>> =
        dao.getIgrejasAtivas(organistaId)

    fun getIgrejasArquivadas(organistaId: Long): Flow<List<Igreja>> =
        dao.getIgrejasArquivadas(organistaId)

    fun getIgrejasByOrganista(organistaId: Long): Flow<List<Igreja>> =
        dao.getIgrejasByOrganista(organistaId)

    suspend fun getIgrejaById(id: Long): Igreja? = dao.getIgrejaById(id)

    suspend fun saveIgreja(igreja: Igreja): Long = dao.insertIgreja(igreja)

    suspend fun updateIgreja(igreja: Igreja) = dao.updateIgreja(igreja)

    suspend fun deleteIgreja(igreja: Igreja) = dao.deleteIgreja(igreja)

    suspend fun arquivarIgreja(id: Long) = dao.setArquivada(id, true)

    suspend fun desarquivarIgreja(id: Long) = dao.setArquivada(id, false)
}
