package io.jeecloud.aidigitalsignage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the application.
 * Configures authentication and authorization rules.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Password encoder bean for encoding passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure default user for development environments.
     * Username: user
     * Password: user
     */
    @Bean
    @Profile({"local", "dev"})
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.builder()
            .username("user")
            .password(passwordEncoder.encode("user"))
            .roles("USER", "ADMIN")
            .build();
        
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Allow public access to Swagger UI and API docs
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                // Allow public access to H2 console (for local development)
                .requestMatchers("/h2-console/**").permitAll()
                // Allow public access to actuator endpoints
                .requestMatchers("/actuator/**").permitAll()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf
                // Disable CSRF for API endpoints (stateless API with Basic Auth)
                .ignoringRequestMatchers("/api/**")
                // Disable CSRF for H2 console
                .ignoringRequestMatchers("/h2-console/**")
            )
            .headers(headers -> headers
                // Allow H2 console to be displayed in frames
                .frameOptions(frame -> frame.sameOrigin())
            )
            .httpBasic(basic -> {
                // Enable HTTP Basic authentication for API endpoints
            });

        return http.build();
    }
}
