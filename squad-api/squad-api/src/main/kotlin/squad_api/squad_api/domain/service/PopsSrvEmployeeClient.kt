package squad_api.squad_api.domain.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import squad_api.squad_api.application.dto.EmployeeDTO

@Service
class PopsSrvEmployeeClient(
    private val restTemplate: RestTemplate,
    @Value("\${pops-srv-employee.url}") private val baseUrl: String
) {
    fun findEmployeeById(id: Long, token: String): EmployeeDTO? {
        val url = "$baseUrl/employees/$id"
        val headers = org.springframework.http.HttpHeaders()
        headers.set("Authorization", token)
        val entity = org.springframework.http.HttpEntity<String>(null, headers)
        return try {
            val response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                entity,
                EmployeeDTO::class.java
            )
            response.body
        } catch (ex: Exception) {
            null
        }
    }
}