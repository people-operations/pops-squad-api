package squad_api.squad_api.domain.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import squad_api.squad_api.application.dto.EmployeeDTO
import squad_api.squad_api.application.dto.EmployeeSkillDTO
import squad_api.squad_api.application.dto.EmployeeSkillTypeDTO

@Service
class PopsSrvEmployeeClient(
    private val restTemplate: RestTemplate,
    @Value("\${pops-srv-employee.url}") private val baseUrl: String
) {
    fun findEmployeeById(id: Long, token: String): EmployeeDTO? {
        // Converter Long para Int se necessário
        val employeeIdInt = if (id > Int.MAX_VALUE) {
            return null
        } else {
            id.toInt()
        }
        
        // A baseUrl já inclui /employees no final, então usamos apenas baseUrl/{id}
        val url = "$baseUrl/$employeeIdInt"
        val headers = org.springframework.http.HttpHeaders()
        headers.set("Authorization", token)
        val entity = org.springframework.http.HttpEntity<String>(null, headers)
        return try {
            val response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                entity,
                EmployeeResponseDTO::class.java
            )
            val employeeResponse = response.body
            if (employeeResponse != null) {
                // Converter para EmployeeDTO
                EmployeeDTO(
                    id = employeeResponse.id.toLong(),
                    name = employeeResponse.name,
                    jobTitle = employeeResponse.jobTitle,
                    contractWage = employeeResponse.contractWage,
                    workHoursPerWeek = employeeResponse.workHoursPerWeek,
                    skills = employeeResponse.skills.map { skill ->
                        EmployeeSkillDTO(
                            id = skill.id,
                            name = skill.name,
                            skillType = skill.skillType?.let {
                                EmployeeSkillTypeDTO(
                                    id = it.id,
                                    name = it.name
                                )
                            }
                        )
                    }
                )
            } else {
                null
            }
        } catch (ex: org.springframework.web.client.HttpClientErrorException) {
            // 404 é esperado em alguns casos
            if (ex.statusCode.value() != 404) {
                println("Erro HTTP ao buscar employee $employeeIdInt: ${ex.statusCode.value()}")
            }
            null
        } catch (ex: Exception) {
            println("Erro ao buscar employee $employeeIdInt: ${ex.message}")
            ex.printStackTrace()
            null
        }
    }
    
    // DTO interno para deserialização da resposta da employee-api
    private data class EmployeeResponseDTO(
        val id: Int,
        val name: String,
        val skills: List<EmployeeSkillResponseDTO> = emptyList(),
        val contractWage: Double? = null,
        val workHoursPerWeek: Int? = null,
        val jobTitle: String? = null
    )
    
    private data class EmployeeSkillResponseDTO(
        val id: Int,
        val name: String,
        val skillType: EmployeeSkillTypeResponseDTO? = null
    )
    
    private data class EmployeeSkillTypeResponseDTO(
        val id: Int? = null,
        val name: String? = null
    )
}