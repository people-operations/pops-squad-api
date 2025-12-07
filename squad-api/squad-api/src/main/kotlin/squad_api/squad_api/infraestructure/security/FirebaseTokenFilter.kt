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

        // 🔹 1) Deixa o preflight CORS passar sem mexer
        if (request.method.equals("OPTIONS", ignoreCase = true)) {
            filterChain.doFilter(request, response)
            return
        }

        val header = request.getHeader("Authorization")

        // 🔹 2) Se não tem Bearer, não tenta autenticar, só segue o fluxo
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = header.removePrefix("Bearer ").trim()

        try {
            val decodedToken = FirebaseAuth.getInstance().verifyIdToken(token)
            val uid = decodedToken.uid

            val authToken = UsernamePasswordAuthenticationToken(uid, null, emptyList())
            SecurityContextHolder.getContext().authentication = authToken
            request.setAttribute("firebaseUid", uid)
        } catch (e: Exception) {
            // 🔹 3) Token inválido:
            // Em vez de mandar 401 aqui, só NÃO autentica.
            // Para endpoints que exigirem auth, o Spring Security vai barrar depois.
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }
}