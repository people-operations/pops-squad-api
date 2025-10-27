package squad_api.squad_api.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import squad_api.squad_api.domain.model.Allocation

interface AllocationRepository: JpaRepository<Allocation, Long>  {
    fun findAllByTeamId(teamId: Long): List<Allocation>
    fun deleteAllByTeamId(teamId: Long)
    fun findAllByPersonId(personId: Long): List<Allocation>
}