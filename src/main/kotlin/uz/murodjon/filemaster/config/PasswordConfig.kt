package uz.murodjon.filemaster.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * The password encoder lives in its own config (not SecurityConfig) to avoid a bean cycle:
 * SecurityConfig needs AuthService (for the auth filter), and AuthService needs the encoder.
 */
@Configuration
class PasswordConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
