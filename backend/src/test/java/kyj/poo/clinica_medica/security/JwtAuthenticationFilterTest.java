package kyj.poo.clinica_medica.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kyj.poo.clinica_medica.model.Credencial;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.model.Papel;
import kyj.poo.clinica_medica.repository.CredencialRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

    private static final String SEGREDO =
        "chave-secreta-para-testes-com-mais-de-32-caracteres";

    private final JwtService jwtService = new JwtService(SEGREDO, 60_000);
    private final CredencialRepository repositorio = mock(
        CredencialRepository.class
    );
    private final CredencialUserDetailsService userDetailsService =
        new CredencialUserDetailsService(repositorio);
    private final JwtAuthenticationFilter filtro = new JwtAuthenticationFilter(
        jwtService,
        userDetailsService
    );

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveAutenticarRequisicaoComTokenValido() throws Exception {
        Paciente paciente = new Paciente("Ana", "ana@clinica.com", 30, null);
        Credencial credencial = new Credencial(
            paciente,
            "senha123",
            Papel.PACIENTE
        );
        String token = jwtService.gerarToken(credencial);

        when(repositorio.findByUsuario_Email("ana@clinica.com")).thenReturn(
            Optional.of(credencial)
        );

        MockHttpServletRequest requisicao = new MockHttpServletRequest();
        requisicao.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse resposta = new MockHttpServletResponse();

        filtro.doFilter(requisicao, resposta, new MockFilterChain());

        var autenticacao =
            SecurityContextHolder.getContext().getAuthentication();
        assertEquals("ana@clinica.com", autenticacao.getName());
        assertTrue(
            autenticacao
                .getAuthorities()
                .stream()
                .anyMatch(autoridade ->
                    autoridade.getAuthority().equals("ROLE_PACIENTE")
                )
        );
    }

    @Test
    void deveIgnorarTokenInvalido() throws Exception {
        MockHttpServletRequest requisicao = new MockHttpServletRequest();
        requisicao.addHeader("Authorization", "Bearer token.invalido");
        MockHttpServletResponse resposta = new MockHttpServletResponse();

        filtro.doFilter(requisicao, resposta, new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
