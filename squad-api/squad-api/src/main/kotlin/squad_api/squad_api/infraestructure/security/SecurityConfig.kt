package squad_api.squad_api.infraestructure.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val firebaseTokenFilter: FirebaseTokenFilter,
    private val corsConfigurationSource: CorsConfigurationSource
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource) }
            .authorizeHttpRequests { auth ->
                // OPTIONS deve ser permitido primeiro, antes de qualquer outra verificação
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                auth.requestMatchers("/public/**").permitAll()
                auth.requestMatchers("/**").permitAll()
                auth.anyRequest().permitAll()
            }
            .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}