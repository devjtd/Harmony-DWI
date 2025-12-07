package com.harmony.sistema.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.harmony.sistema.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    private static final String[] PUBLIC_ROUTES = {
            "/", "/acerca", "/profesores", "/inscripcion", "/talleres",
            "/blog", "/pago", "/contacto/**", "/confirmacion",
            "/taller/**", "/profesor/**",
            "/css/**", "/js/**", "/images/**", "/media/**",
            "/*.js", "/*.css", "/*.html", "/*.ico", "/*.json", "/*.txt",
            "/*.jpg", "/*.png", "/chunk-*.js", "/main-*.js", "/polyfills-*.js",
            "/scripts-*.js", "/styles-*.css"
    };

    // Configura la cadena de filtros de seguridad HTTP
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("[INFO] [CONFIG] Inicializando Bean: SecurityFilterChain (Configuración de Seguridad HTTP)");

        http
                // Deshabilita CSRF y configura autorización
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_ROUTES).permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/horario", "/cambiar-clave").hasAnyRole("CLIENTE", "PROFESOR")
                        .anyRequest().authenticated())

                // Configura proveedor y filtro JWT
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // Deshabilita formLogin para REST API - usa JWT en su lugar
                // .formLogin(form -> form
                // .loginPage("/login")
                // .successHandler(mySuccessHandler())
                // .usernameParameter("email")
                // .passwordParameter("password")
                // .permitAll())

                // Configura manejo de excepciones para REST API
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(401, "Unauthorized");
                        }))

                .logout(logout -> logout.permitAll())

                // Configura gestión de sesiones para REST API
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    // Maneja redirección post-login según rol
    @Bean
    public AuthenticationSuccessHandler mySuccessHandler() {
        System.out
                .println(
                        "[INFO] [CONFIG] Inicializando Bean: AuthenticationSuccessHandler (Manejo de redirección por Rol)");
        return (request, response, authentication) -> {
            var roles = authentication.getAuthorities();

            // Redirige según el rol detectado
            if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
                System.out.println("[INFO] [AUTH] Login exitoso. Redirigiendo a /admin/clientes.");
                response.sendRedirect("/admin/clientes");
            } else if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_CLIENTE"))) {
                System.out.println("[INFO] [AUTH] Login exitoso. Redirigiendo a /horario (Cliente).");
                response.sendRedirect("/horario");
            } else if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_PROFESOR"))) {
                System.out.println("[INFO] [AUTH] Login exitoso. Redirigiendo a /horario (Profesor).");
                response.sendRedirect("/horario");
            } else {
                System.out.println("[WARN] [AUTH] Login exitoso. Rol no reconocido. Redirigiendo a /login.");
                response.sendRedirect("/login");
            }
        };
    }
}
