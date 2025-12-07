package squad_api.squad_api.application.controller

import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import squad_api.squad_api.application.dto.TeamCreateRequest
import squad_api.squad_api.application.dto.TeamResponseDTO
import squad_api.squad_api.domain.model.Team
import squad_api.squad_api.domain.service.AllocationDeleteService
import squad_api.squad_api.domain.service.AllocationHistoryService
import squad_api.squad_api.domain.service.AllocationService
import squad_api.squad_api.domain.service.TeamService

@RestController
@RequestMapping("/teams")
class TeamController(
    private val teamService: TeamService,
    private val allocationService: AllocationService,
    private val allocationHistoryService: AllocationHistoryService,
    private val allocationDeleteService: AllocationDeleteService
) {
    @GetMapping
    fun list(
        pageable: Pageable,
        @RequestHeader("Authorization") authHeader: String,
    ): ResponseEntity<Any> = try {
        val allocations = allocationService.findAll()
        ResponseEntity.ok(teamService.findAllTeamsPageable(pageable, authHeader, allocations))
    } catch (ex: ResponseStatusException) {
        ResponseEntity.status(ex.statusCode).body(mapOf("error" to ex.reason))
    }

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: Long,
        @RequestHeader("Authorization") authHeader: String,
    ): ResponseEntity<Any> = try {
        val allocations = allocationService.findAllByTeamId(id, authHeader)
        ResponseEntity.ok(teamService.findTeamById(id, authHeader, allocations))
    } catch (ex: ResponseStatusException) {
        ResponseEntity.status(ex.statusCode).body(mapOf("error" to ex.reason))
    }

    @GetMapping("/project/{projectId}")
    fun getByProjectId(
        @PathVariable projectId: Long,
        @RequestHeader("Authorization") authHeader: String,
    ): ResponseEntity<Any> = try {
        println("TeamController.getByProjectId: Buscando teams para projeto $projectId")
        val teams = teamService.findAllTeamsByProjectId(projectId, authHeader)
        println("TeamController.getByProjectId: Retornando ${teams.size} teams para projeto $projectId")
        if (teams.isEmpty()) {
            ResponseEntity.ok(emptyList<TeamResponseDTO>())
        } else {
            ResponseEntity.ok(teams)
        }
    } catch (ex: ResponseStatusException) {
        println("TeamController.getByProjectId: Erro ao buscar teams - ${ex.statusCode} - ${ex.reason}")
        ResponseEntity.status(ex.statusCode).body(mapOf("error" to ex.reason))
    } catch (ex: Exception) {
        println("TeamController.getByProjectId: Exceção inesperada - ${ex.message}")
        ex.printStackTrace()
        ResponseEntity.status(500).body(mapOf("error" to ex.message))
    }

    @PostMapping
    fun create(@RequestBody request: TeamCreateRequest): Team {
        val entity = Team(
            name = request.name,
            description = request.description,
            sprintDuration = request.sprintDuration,
            approverId = request.approverId,
            projectId = request.projectId,
            status = request.status,
        )
        return teamService.save(entity)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: TeamCreateRequest): ResponseEntity<Any> = try {
        val entity = Team(
            id = id,
            name = request.name,
            description = request.description,
            sprintDuration = request.sprintDuration,
            approverId = request.approverId,
            projectId = request.projectId,
            status = request.status
        )
        ResponseEntity.ok(teamService.update(id, entity))
    } catch (ex: ResponseStatusException) {
        ResponseEntity.status(ex.statusCode).body(mapOf("error" to ex.reason))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Any> = try {
        val currentAllocations = allocationService.findAllByTeamId(id, "")

        allocationHistoryService.saveCurrentAllocationsAsHistory(currentAllocations)
        allocationDeleteService.deleteAllByTeamId(id)
        teamService.softDeleteTeam(id)

        ResponseEntity.noContent().build()
    } catch (ex: ResponseStatusException) {
        ResponseEntity.status(ex.statusCode).body(mapOf("error" to ex.reason))
    }
}
