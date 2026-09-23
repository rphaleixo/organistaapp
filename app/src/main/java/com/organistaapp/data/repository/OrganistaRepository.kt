package com.organistaapp.data.repository

import com.organistaapp.data.local.OrganistaDao
import com.organistaapp.data.model.Organista
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrganistaRepository @Inject constructor(
    private val dao: OrganistaDao
) {
    fun getAllOrganistas(): Flow<List<Organista>> = dao.getAllOrganistas()

    fun getOrganistaPrincipal(): Flow<Organista?> = dao.getOrganistaPrincipal()

    suspend fun getOrganistaById(id: Long): Organista? = dao.getOrganistaById(id)

    suspend fun saveOrganista(organista: Organista): Long = dao.insertOrganista(organista)

    suspend fun updateOrganista(organista: Organista) = dao.updateOrganista(organista)

    suspend fun deleteOrganista(organista: Organista) = dao.deleteOrganista(organista)
}
