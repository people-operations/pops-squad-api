package squad_api.squad_api.application.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import squad_api.squad_api.application.dto.AllocationCreateRequest
import squad_api.squad_api.application.dto.AllocationResponse
import squad_api.squad_api.domain.model.Allocation
import squad_api.squad_api.domain.service.AllocationService

@RestController
@RequestMapping("/teams")
class AllocationController(
    private val allocationService: AllocationService,
) {
    @PostMapping("/{teamId}/allocations")
    fun create(
        @PathVariable teamId: Long,
        @RequestBody request: AllocationCreateRequest,
        @RequestHeader("Authorization") authHeader: String,
    ): Allocation {
        return try {
            allocationService.replaceAllocations(teamId, request, authHeader)
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao criar alocação", ex)
        }
    }

    @GetMapping("/{teamId}/allocations")
    fun list(
        @PathVariable teamId: Long,
        @RequestHeader("Authorization") authHeader: String
    ): List<AllocationResponse> {
        return allocationService.findAllByTeamId(teamId, authHeader)
    }

    @GetMapping("/allocations/person/{id}")
    fun findAllocationsByPersonId(
        @PathVariable id: Long,
        @RequestHeader("Authorization") authHeader: String
    ): List<AllocationResponse> {
        return allocationService.findAllByPersonId(id, authHeader)
    }
}