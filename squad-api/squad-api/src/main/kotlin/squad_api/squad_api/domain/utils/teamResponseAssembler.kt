package squad_api.squad_api.domain.utils

import org.springframework.stereotype.Component
import squad_api.squad_api.application.dto.ApproverDTO
import squad_api.squad_api.application.dto.TeamResponseDTO
import squad_api.squad_api.domain.model.Allocation
import squad_api.squad_api.domain.model.Team
import squad_api.squad_api.domain.repository.AllocationRepository
import squad_api.squad_api.domain.service.PopsSrvEmployeeClient

@Component
class TeamResponseAssembler(
    private val popsSrvEmployeeClient: PopsSrvEmployeeClient,
    private val allocationRepository: AllocationRepository
) {

    fun toTeamResponseDTO(
        team: Team,
        token: String,
        allocations: List<Allocation>
    ): TeamResponseDTO {
        val approverDTO = buildApproverDTO(team, token)

        val teamAllocations = filterAllocationsForTeam(team, allocations)
        val memberCount = teamAllocations.size
        val allocatedHours = teamAllocations.sumOf { it.allocatedHours }
        val totalHours = team.sprintDuration * 40
        val capacity = totalHours

        println(
            "TeamResponseAssembler: Team ${team.id} - " +
                    "sprintDuration=${team.sprintDuration}, memberCount=$memberCount, " +
                    "allocatedHours=$allocatedHours, totalHours=$totalHours"
        )

        return TeamResponseDTO(
            id = team.id!!,
            name = team.name,
            description = team.description,
            sprintDuration = team.sprintDuration,
            approverId = team.approverId,
            projectId = team.projectId,
            status = team.status,
            approver = approverDTO,
            memberCount = memberCount,
            allocatedHours = allocatedHours,
            totalHours = totalHours,
            capacity = capacity
        )
    }

    private fun buildApproverDTO(team: Team, token: String): ApproverDTO? {
        val approverId = team.approverId ?: return null
        val approverFull = popsSrvEmployeeClient.findEmployeeById(approverId, token) ?: return null
        return ApproverDTO(id = approverFull.id, name = approverFull.name)
    }

    private fun filterAllocationsForTeam(
        team: Team,
        allAllocations: List<Allocation>
    ): List<Allocation> {
        if (allAllocations.isNotEmpty()) {
            return allAllocations.filter { it.team.id == team.id }
        }
        return allocationRepository.findAllByTeamId(team.id!!)
    }
}