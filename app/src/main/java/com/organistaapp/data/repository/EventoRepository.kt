package com.organistaapp.data.repository

import com.organistaapp.data.local.EventoDao
import com.organistaapp.data.model.Evento
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventoRepository @Inject constructor(
    private val dao: EventoDao
) {
    fun getEventosByOrganista(organistaId: Long): Flow<List<Evento>> =
        dao.getEventosByOrganista(organistaId)

    fun getEventosByMes(organistaId: Long, inicioMes: Long, fimMes: Long): Flow<List<Evento>> =
        dao.getEventosByMes(organistaId, inicioMes, fimMes)

    fun getProximosEventos(agora: Long): Flow<List<Evento>> = dao.getProximosEventos(agora)

    suspend fun getEventoById(id: Long): Evento? = dao.getEventoById(id)

    suspend fun saveEvento(evento: Evento): Long = dao.insertEvento(evento)

    suspend fun updateEvento(evento: Evento) = dao.updateEvento(evento)

    suspend fun deleteEvento(evento: Evento) = dao.deleteEvento(evento)

    suspend fun deleteEventosByEscala(escalaId: Long) = dao.deleteEventosByEscala(escalaId)
}
