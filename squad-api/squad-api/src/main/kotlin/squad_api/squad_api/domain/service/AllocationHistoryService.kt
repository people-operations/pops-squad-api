package squad_api.squad_api.domain.service

import org.springframework.stereotype.Service
import squad_api.squad_api.application.dto.AllocationResponse
import squad_api.squad_api.domain.model.AllocationHistory
import squad_api.squad_api.domain.repository.AllocationHistoryRepository
import squad_api.squad_api.infraestructure.utilities.CrudService

@Service
class AllocationHistoryService (
    private val allocationHistoryRepository: AllocationHistoryRepository
    , private val teamService: TeamService
) : CrudService<AllocationHistory, Long>() {

    override val repository = allocationHistoryRepository

    fun saveCurrentAllocationsAsHistory(currentAllocations: List<AllocationResponse>) {
        currentAllocations.forEach { allocation ->
            val team = teamService.findById(allocation.team.id)
            val saveHistory = AllocationHistory(
                startedAt = allocation.startedAt,
                endedAt = java.time.LocalDate.now(),
                team = team,
                personId = allocation.personId!!,
                position = allocation.position, // Assuming employee name is not stored in AllocationHistory
                allocatedHours = allocation.allocatedHours,
            )
            allocationHistoryRepository.save(saveHistory)
        }
    }
}