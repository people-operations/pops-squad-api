package squad_api.squad_api.application.controller

import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import squad_api.squad_api.application.dto.TeamCreateRequest
import squad_api.squad_api.domain.model.Team
import squad_api.squad_api.domain.service.TeamService

@RestController
@RequestMapping("/teams")
class TeamController(
    private val teamService: TeamService
) {
    @GetMapping
    fun list(pageable: Pageable) = teamService.findAllPageable(pageable)

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<Any> = try {
        ResponseEntity.ok(teamService.findById(id))
    } catch (ex: ResponseStatusException) {
        ResponseEntity.status(ex.statusCode).body(mapOf("error" to ex.reason))
    }

    @PostMapping
    fun create(@RequestBody request: TeamCreateRequest): Team {
        val entity = Team(
            name = request.name,
            description = request.description,
            sprintDuration = request.sprintDuration,
            approverId = request.approverId,
            projectId = request.projectId
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
            projectId = request.projectId
        )
        ResponseEntity.ok(teamService.update(id, entity))
    } catch (ex: ResponseStatusException) {
        ResponseEntity.status(ex.statusCode).body(mapOf("error" to ex.reason))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Any> = try {
        teamService.delete(id)
        ResponseEntity.noContent().build()
    } catch (ex: ResponseStatusException) {
        ResponseEntity.status(ex.statusCode).body(mapOf("error" to ex.reason))
    }
}
