package com.apirest.apirestfull.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.apirest.apirestfull.security.jwt.JwtEntryPoint;
import com.apirest.apirestfull.security.jwt.JwTokenFilter;
import com.apirest.apirestfull.security.service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtEntryPoint jwtEntryPoint;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService, JwtEntryPoint jwtEntryPoint) {
        this.userDetailsService = userDetailsService;
        this.jwtEntryPoint = jwtEntryPoint;
    }

    @Bean
    public JwTokenFilter jwTokenFilter() {
        return new JwTokenFilter();
    }

    // Encriptación de contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configuración del AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Configuración de las reglas de seguridad HTTP
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // Deshabilita CSRF por simplicidad
            .authorizeRequests()
                .requestMatchers("/auth/**").permitAll() // Rutas permitidas sin autenticación
                .anyRequest().authenticated() // Cualquier otra solicitud requiere autenticación
            .and()
            .exceptionHandling().authenticationEntryPoint(jwtEntryPoint) // Manejo de errores
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // Sin sesiones en el servidor
        
        // Agrega el filtro de JWT antes del filtro de autenticación por defecto
        http.addFilterBefore(jwTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
