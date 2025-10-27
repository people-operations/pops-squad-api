package squad_api.squad_api.infraestructure.security

import com.google.firebase.auth.FirebaseAuth
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class FirebaseTokenFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")

        if (header != null && header.startsWith("Bearer ")) {
            val token = header.removePrefix("Bearer ").trim()
            try {
                val decodedToken = FirebaseAuth.getInstance().verifyIdToken(token)
                val uid = decodedToken.uid
                val authToken = UsernamePasswordAuthenticationToken(uid, null, emptyList())
                SecurityContextHolder.getContext().authentication = authToken
                request.setAttribute("firebaseUid", uid)
            } catch (e: Exception) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token Firebase inválido")
                return
            }
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token ausente")
            return
        }

        filterChain.doFilter(request, response)
    }
}