package kyj.poo.clinica_medica.security;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(
            List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );
        configuration.setAllowedHeaders(
            List.of("Authorization", "Content-Type")
        );
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry
                    .addMapping("/**")
                    .allowedOrigins("http://localhost:5173")
                    .allowedMethods(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                    )
                    .allowedHeaders("Authorization", "Content-Type")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http
    ) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(ex ->
                ex
                    .authenticationEntryPoint((req, res, exce) ->
                        res.sendError(401, "Não autenticado")
                    )
                    .accessDeniedHandler((req, res, exce) ->
                        res.sendError(403, "Acesso negado")
                    )
            )
            .authorizeHttpRequests(auth ->
                auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(
                        "/api/auth/**",
                        "/api/planos",
                        "/api/especialidades",
                        "/login",
                        "/error",
                        "/h2-console/**"
                    )
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/consultas")
                    .hasAnyRole("ADMIN", "PACIENTE")
                    .requestMatchers(
                        HttpMethod.POST,
                        "/api/consultas/*/cancelar"
                    )
                    .hasAnyRole("ADMIN", "PACIENTE", "MEDICO")
                    .requestMatchers(
                        HttpMethod.POST,
                        "/api/consultas/*/realizar"
                    )
                    .hasAnyRole("ADMIN", "MEDICO")
                    // Cada usuário altera apenas os próprios dados (§4)
                    .requestMatchers(HttpMethod.PUT, "/api/pacientes/me")
                    .hasRole("PACIENTE")
                    .requestMatchers(HttpMethod.PUT, "/api/medicos/me")
                    .hasRole("MEDICO")
                    // Paciente paga a própria conta (§9)
                    .requestMatchers(HttpMethod.POST, "/api/contas/*/pagar")
                    .hasAnyRole("ADMIN", "PACIENTE")
                    // Paciente avalia a consulta realizada (§10)
                    .requestMatchers(HttpMethod.POST, "/api/avaliacoes")
                    .hasRole("PACIENTE")
                    // Médico registra o prontuário (§7)
                    .requestMatchers(HttpMethod.POST, "/api/prontuarios")
                    .hasAnyRole("ADMIN", "MEDICO")
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/medicos",
                        "/api/medicos/**",
                        "/api/pacientes",
                        "/api/pacientes/**",
                        "/api/consultas",
                        "/api/consultas/**",
                        "/api/contas",
                        "/api/contas/**"
                    )
                    .authenticated()
                    .requestMatchers(
                        "/api/medicos",
                        "/api/medicos/**",
                        "/api/pacientes",
                        "/api/pacientes/**",
                        "/api/consultas",
                        "/api/consultas/**",
                        "/api/contas",
                        "/api/contas/**"
                    )
                    .hasRole("ADMIN")
                    .requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/medico/**")
                    .hasRole("MEDICO")
                    .requestMatchers("/paciente/**")
                    .hasRole("PACIENTE")
                    .anyRequest()
                    .authenticated()
            )
            .logout(logout ->
                logout.logoutSuccessUrl("/login?logout").permitAll()
            )
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
