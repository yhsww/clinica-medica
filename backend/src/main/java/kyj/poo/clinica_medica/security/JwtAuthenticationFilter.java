package kyj.poo.clinica_medica.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;
    private final CredencialUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
        JwtService jwtService,
        CredencialUserDetailsService userDetailsService
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest requisicao,
        HttpServletResponse resposta,
        FilterChain cadeia
    ) throws ServletException, IOException {
        String cabecalho = requisicao.getHeader("Authorization");

        if (cabecalho != null && cabecalho.startsWith(PREFIXO)) {
            autenticar(cabecalho.substring(PREFIXO.length()).trim());
        }

        cadeia.doFilter(requisicao, resposta);
    }

    private void autenticar(String token) {
        try {
            String email = jwtService.extrairEmail(token);

            if (
                SecurityContextHolder.getContext().getAuthentication() == null
            ) {
                UserDetails usuario = userDetailsService.loadUserByUsername(
                    email
                );

                if (usuario.isEnabled()) {
                    var autenticacao = new UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        usuario.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(
                        autenticacao
                    );
                }
            }
        } catch (
            JwtException
            | UsernameNotFoundException
            | IllegalArgumentException excecao
        ) {
            SecurityContextHolder.clearContext();
        }
    }
}
