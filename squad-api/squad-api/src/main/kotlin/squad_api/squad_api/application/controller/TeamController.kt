package squad_api.squad_api.application.controller

import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*
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
    fun get(@PathVariable id: Long) = teamService.findById(id)

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
    fun update(@PathVariable id: Long, @RequestBody request: TeamCreateRequest): Team {
        val entity = Team(
            id = id,
            name = request.name,
            description = request.description,
            sprintDuration = request.sprintDuration,
            approverId = request.approverId,
            projectId = request.projectId
        )
        return teamService.update(id, entity)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = teamService.delete(id)
}
