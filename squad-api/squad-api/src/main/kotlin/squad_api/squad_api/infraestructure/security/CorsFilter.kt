package squad_api.squad_api.infraestructure.security

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorsFilter : Filter {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        val origin = httpRequest.getHeader("Origin")
        val referer = httpRequest.getHeader("Referer")
        
        // Determina se a requisição vem de localhost:3000
        val isLocalhost3000 = origin == "http://localhost:3000" || 
                              (origin != null && origin.startsWith("http://localhost:3000")) ||
                              (referer != null && referer.startsWith("http://localhost:3000"))
        
        // Sempre adiciona headers CORS para localhost:3000 ou se não houver Origin (pode ser preflight)
        if (isLocalhost3000 || origin == null || "OPTIONS".equals(httpRequest.method, ignoreCase = true)) {
            val allowedOrigin = if (origin != null && (origin == "http://localhost:3000" || origin.startsWith("http://localhost:3000"))) {
                origin
            } else {
                "http://localhost:3000"
            }
            httpResponse.setHeader("Access-Control-Allow-Origin", allowedOrigin)
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true")
            httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD")
            httpResponse.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With, Accept, Origin")
            httpResponse.setHeader("Access-Control-Max-Age", "3600")
            httpResponse.setHeader("Access-Control-Expose-Headers", "*")
        }

        // Trata preflight OPTIONS imediatamente
        if ("OPTIONS".equals(httpRequest.method, ignoreCase = true)) {
            httpResponse.status = HttpServletResponse.SC_OK
            return
        }

        chain.doFilter(request, response)
    }
}

