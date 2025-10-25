package squad_api.squad_api.domain.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import squad_api.squad_api.domain.repository.AllocationRepository

@Service
class AllocationDeleteService(
    private val allocationRepository: AllocationRepository
) {
    @Transactional
    fun deleteAllByTeamId(teamId: Long) {
        allocationRepository.deleteAllByTeamId(teamId)
    }
}