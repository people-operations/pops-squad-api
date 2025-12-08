package squad_api.squad_api.domain.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import squad_api.squad_api.application.dto.ApproverDTO
import squad_api.squad_api.application.dto.TeamResponseDTO
import squad_api.squad_api.application.dto.TeamDetailDTO
import squad_api.squad_api.application.dto.MemberDetailDTO
import squad_api.squad_api.application.dto.MemberSkillDetailDTO
import squad_api.squad_api.application.dto.SkillDetailDTO
import squad_api.squad_api.domain.model.Team
import squad_api.squad_api.domain.repository.TeamRepository
import squad_api.squad_api.domain.repository.AllocationRepository
import squad_api.squad_api.infraestructure.utilities.CrudService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import squad_api.squad_api.domain.model.Allocation
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class TeamService(
    private val teamRepository: TeamRepository,
    private val popsSrvEmployeeClient: PopsSrvEmployeeClient,
    private val allocationRepository: AllocationRepository

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
        val approverFull = team.approverId?.let { popsSrvEmployeeClient.findEmployeeById(it, token) }
        val approverDTO = approverFull?.let { ApproverDTO(id = it.id, name = it.name) }
        
        // Buscar allocations do team para calcular memberCount e allocatedHours
        val allocations = allocationRepository.findAllByTeamId(team.id!!)
        val memberCount = allocations.size
        val allocatedHours = allocations.sumOf { it.allocatedHours }
        val totalHours = team.sprintDuration * 40
        val capacity = totalHours
        
        println("TeamService.toTeamResponseDTO: Team ${team.id} - sprintDuration=${team.sprintDuration}, memberCount=$memberCount, allocatedHours=$allocatedHours, totalHours=$totalHours")
        
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

    fun findAllTeamsByProjectId(projectId: Long, token: String): List<TeamResponseDTO> {
        val teams = teamRepository.findAllByProjectId(projectId)
        println("TeamService.findAllTeamsByProjectId: Projeto $projectId - Encontrados ${teams.size} teams")
        return teams.map { toTeamResponseDTO(it, token) }
    }

    fun getTeamDetails(teamId: Long, token: String): TeamDetailDTO {
        try {
            val team = repository.findById(teamId).orElseThrow {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum time encontrado com ID: $teamId")
            }
            
            val allocations = allocationRepository.findAllByTeamId(teamId)
        
        val membersDetail = allocations.mapNotNull { allocation ->
            try {
                // Buscar employee completo para obter salário e horas semanais
                val employee = popsSrvEmployeeClient.findEmployeeById(allocation.personId, token)
                
                if (employee == null) {
                    return@mapNotNull null
                }
                
                // Calcular valor por hora
                val weeklyHours = employee.workHoursPerWeek ?: 40
                val monthlyHoursDecimal = BigDecimal(weeklyHours * 4).setScale(2, RoundingMode.HALF_UP)
                
                val contractWageDecimal = employee.contractWage?.let { 
                    BigDecimal(it).setScale(2, RoundingMode.HALF_UP) 
                }
                
                val hourlyRate = if (contractWageDecimal != null && monthlyHoursDecimal > BigDecimal.ZERO) {
                    contractWageDecimal
                        .divide(monthlyHoursDecimal, 2, RoundingMode.HALF_UP)
                } else {
                    null
                }
                
                // Calcular valor investido no projeto (hourlyRate * allocatedHours)
                val investedValue = if (hourlyRate != null && allocation.allocatedHours > 0) {
                    hourlyRate.multiply(BigDecimal(allocation.allocatedHours))
                        .setScale(2, RoundingMode.HALF_UP)
                } else {
                    null
                }
                
                // Mapear skills do employee
                val memberSkills = employee.skills.map { skill ->
                    MemberSkillDetailDTO(
                        id = skill.id,
                        name = skill.name,
                        skillType = skill.skillType?.name
                    )
                }
                
                MemberDetailDTO(
                    id = employee.id,
                    name = employee.name,
                    jobTitle = employee.jobTitle,
                    allocatedHours = allocation.allocatedHours,
                    skills = memberSkills,
                    contractWage = contractWageDecimal,
                    workHoursPerWeek = employee.workHoursPerWeek,
                    monthlyHours = monthlyHoursDecimal,
                    hourlyRate = hourlyRate,
                    investedValue = investedValue
                )
            } catch (e: Exception) {
                println("Erro ao processar membro ${allocation.personId}: ${e.message}")
                e.printStackTrace()
                null
            }
        }
        
        // Calcular valor total investido no team
        val totalInvestedValue = membersDetail
            .mapNotNull { it.investedValue }
            .fold(BigDecimal.ZERO) { acc, value -> acc.add(value) }
            .setScale(2, RoundingMode.HALF_UP)
        
        // Agregar todas as skills dos membros
        val allSkillsMap = mutableMapOf<Int, SkillDetailDTO>()
        membersDetail.forEach { member ->
            member.skills.forEach { skill ->
                if (!allSkillsMap.containsKey(skill.id)) {
                    allSkillsMap[skill.id] = SkillDetailDTO(
                        id = skill.id,
                        name = skill.name,
                        skillType = skill.skillType
                    )
                }
            }
        }
        val aggregatedSkills = allSkillsMap.values.toList()
        
        val approverFull = team.approverId?.let { popsSrvEmployeeClient.findEmployeeById(it, token) }
        val approverName = approverFull?.name
        
        // Calcular horas totais e alocadas
        val totalHours = team.sprintDuration * 40 // Horas totais baseado no sprintDuration
        val allocatedHours = membersDetail.sumOf { it.allocatedHours }
            
            return TeamDetailDTO(
                teamId = team.id!!,
                teamName = team.name,
                teamDescription = team.description,
                po = approverName,
                membersCount = membersDetail.size,
                members = membersDetail,
                totalInvestedValue = totalInvestedValue,
                skills = aggregatedSkills,
                totalHours = totalHours,
                allocatedHours = allocatedHours,
                sprintDuration = team.sprintDuration
            )
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            println("Erro ao buscar detalhes do team $teamId: ${e.message}")
            e.printStackTrace()
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao buscar detalhes do team: ${e.message}")
        }
    }
}

