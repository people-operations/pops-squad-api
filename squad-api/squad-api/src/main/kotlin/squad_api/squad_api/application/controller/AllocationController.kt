package squad_api.squad_api.application.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import squad_api.squad_api.application.dto.AllocationCreateRequest
import squad_api.squad_api.application.dto.AllocationResponse
import squad_api.squad_api.domain.model.Allocation
import squad_api.squad_api.domain.service.AllocationService
import squad_api.squad_api.domain.service.PopsSrvEmployeeClient
import squad_api.squad_api.domain.utils.AllocationNormalizer

@RestController
@RequestMapping("/teams")
class AllocationController(
    private val allocationService: AllocationService,
    private val popsSrvEmployeeClient: PopsSrvEmployeeClient
) {
    @PostMapping("/{teamId}/allocations")
    fun create(
        @PathVariable teamId: Long,
        @RequestBody request: List<AllocationCreateRequest>,
        @RequestHeader("Authorization") authHeader: String,
    ): List<Allocation> {
        return allocationService.replaceAllocations(teamId, request, authHeader)
    }

    @GetMapping("/{teamId}/allocations")
    fun list(
        @PathVariable teamId: Long,
        @RequestHeader("Authorization") authHeader: String
    ): List<AllocationResponse> {
        val allocations = allocationService.findAllByTeamId(teamId, authHeader)
        val normalizer = AllocationNormalizer(popsSrvEmployeeClient)
        return allocations.map { normalizer.normalize(it, authHeader) }
    }

    @GetMapping("/allocations/person/{id}")
    fun findAllocationsByPersonId(
        @PathVariable id: Long,
        @RequestHeader("Authorization") authHeader: String
    ): List<AllocationResponse> {
        val allocations = allocationService.findAllByPersonId(id, authHeader)
        val normalizer = AllocationNormalizer(popsSrvEmployeeClient)
        return allocations.map { normalizer.normalize(it, authHeader) }
    }
}