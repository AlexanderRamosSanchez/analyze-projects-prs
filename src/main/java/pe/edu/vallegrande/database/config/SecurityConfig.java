package pe.edu.vallegrande.database.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                        .pathMatchers("/swagger-ui.html", "/api/v1/families/active", "/api/v1/families/{id}").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )
                .cors(cors -> cors
                        .configurationSource(exchange -> {
                            var config = new CorsConfiguration();
                            config.setAllowCredentials(true);
                            // Permitir cualquier origen
                            config.setAllowedOriginPatterns(List.of("*"));
                            config.addAllowedHeader("*");
                            config.addAllowedMethod("*");
                            return config;
                        })
                )
                .build();
    }
}