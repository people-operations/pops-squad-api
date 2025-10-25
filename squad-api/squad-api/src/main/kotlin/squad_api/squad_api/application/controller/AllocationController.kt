package squad_api.squad_api.application.controller

import org.springframework.web.bind.annotation.*
import squad_api.squad_api.application.dto.AllocationCreateRequest
import squad_api.squad_api.application.dto.AllocationResponse
import squad_api.squad_api.domain.model.Allocation
import squad_api.squad_api.domain.service.AllocationService

@RestController
@RequestMapping("/teams/{teamId}/allocations")
class AllocationController(
    private val allocationService: AllocationService,
) {
    @PostMapping
    fun create(
        @PathVariable teamId: Long,
        @RequestBody request: AllocationCreateRequest
    ): Allocation {
        return allocationService.replaceAllocations(teamId, request)
    }

    @GetMapping
    fun list(
        @PathVariable teamId: Long
    ): List<AllocationResponse> {
        return allocationService.findAllByTeamId(teamId)
    }

}