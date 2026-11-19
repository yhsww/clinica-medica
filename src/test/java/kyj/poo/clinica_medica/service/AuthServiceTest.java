package kyj.poo.clinica_medica.service;

import kyj.poo.clinica_medica.model.Credencial;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private final CredencialService credencialService =
            mock(CredencialService.class);

    private final AuthService authService =
            new AuthService(credencialService);

    @Test
    void deveAutenticarComCredenciaisValidas() {

        String email = "teste@email.com";
        String senha = "123456";

        Credencial credencial = mock(Credencial.class);

        when(credencialService.autenticar(email, senha))
                .thenReturn(true);

        when(credencialService.buscarPorEmail(email))
                .thenReturn(credencial);

        Credencial resultado =
                authService.autenticar(email, senha);

        assertNotNull(resultado);
        assertEquals(credencial, resultado);

        verify(credencialService)
                .autenticar(email, senha);

        verify(credencialService)
                .buscarPorEmail(email);
    }

    @Test
    void deveRejeitarCredenciaisInvalidas() {

        String email = "teste@email.com";
        String senha = "senhaErrada";

        when(credencialService.autenticar(email, senha))
                .thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authService.autenticar(email, senha)
                );

        assertEquals(
                "E-mail ou senha inválidos.",
                exception.getMessage()
        );

        verify(credencialService)
                .autenticar(email, senha);

        verify(credencialService, never())
                .buscarPorEmail(email);
    }
}