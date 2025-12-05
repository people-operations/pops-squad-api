package squad_api.squad_api.domain.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import squad_api.squad_api.application.dto.ApproverDTO
import squad_api.squad_api.application.dto.TeamResponseDTO
import squad_api.squad_api.domain.model.Team
import squad_api.squad_api.domain.repository.TeamRepository
import squad_api.squad_api.infraestructure.utilities.CrudService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import squad_api.squad_api.domain.model.Allocation

@Service
class TeamService(
    private val teamRepository: TeamRepository,
    private val popsSrvEmployeeClient: PopsSrvEmployeeClient,

) : CrudService<Team, Long>() {

    override val repository = teamRepository


    fun findTeamById(
        teamId: Long,
        token: String,
        allocations: List<Allocation>
    ): TeamResponseDTO {
        val team = repository.findById(teamId)
        if (team.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum time encontrado com ID: $teamId")
        }
        val teamAllocations = allocations.filter { it.team.id == teamId }
        val allocatedPeopleCount = teamAllocations.size
        val totalAllocatedHours = teamAllocations.sumOf { it.allocatedHours }
        return toTeamResponseDTO(team.get(), token, allocatedPeopleCount, totalAllocatedHours)
    }

    fun toTeamResponseDTO(
        team: Team,
        token: String,
        allocatedPeopleCount: Int? = null,
        totalAllocatedHours: Int? = null
    ): TeamResponseDTO {
        val approver = team.approverId?.let { popsSrvEmployeeClient.findEmployeeById(it, token) }
        val approverDTO = approver?.let { ApproverDTO(id = it.id, name = it.name) }

        return TeamResponseDTO(
            id = team.id!!,
            name = team.name,
            description = team.description,
            sprintDuration = team.sprintDuration,
            approverId = team.approverId,
            projectId = team.projectId,
            status = team.status,
            approver = approverDTO,
            allocatedPeopleCount = allocatedPeopleCount!!,
            totalAllocatedHours = totalAllocatedHours!!
        )
    }

    fun findAllTeamsPageable(
        pageable: Pageable,
        token: String,
        allocations: List<Allocation>
    ): Page<TeamResponseDTO> {
        val teamsPage = teamRepository.findAllByStatusTrue(pageable)
        return teamsPage.map { team ->
            val teamAllocations = allocations.filter { it.team.id == team.id }
            val allocatedPeopleCount = teamAllocations.size
            val totalAllocatedHours = teamAllocations.sumOf { it.allocatedHours }
            toTeamResponseDTO(team, token, allocatedPeopleCount, totalAllocatedHours)
        }
    }

    fun softDeleteTeam(id: Long) {
        val team = repository.findById(id).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Time não encontrado com id: $id")
        }
        team.status = false
        repository.save(team)
    }
}

