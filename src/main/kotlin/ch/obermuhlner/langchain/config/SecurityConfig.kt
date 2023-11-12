package ch.obermuhlner.langchain.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeHttpRequests { it.requestMatchers("/api/users/**").hasRole("ADMIN") }
            .authorizeHttpRequests { it.requestMatchers("/api/chats/**").hasRole("USER") }
            .authorizeHttpRequests { it.requestMatchers("/api/assistants/**").hasRole("USER") }
            .authorizeHttpRequests { it.requestMatchers("/api/assistants/**").hasRole("USER") }
            .authorizeHttpRequests { it.requestMatchers("/api/documents/**").hasRole("USER") }
            .authorizeHttpRequests { it.requestMatchers("/api/document-segments/**").hasRole("USER") }
            .authorizeHttpRequests { it.anyRequest().denyAll() }
            .csrf { it.ignoringRequestMatchers("/api/**") }
            .httpBasic(Customizer.withDefaults())
            .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return NoOpPasswordEncoder.getInstance()
    }
}