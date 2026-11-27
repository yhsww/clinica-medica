package kyj.poo.clinica_medica.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import kyj.poo.clinica_medica.model.Credencial;
import kyj.poo.clinica_medica.repository.CredencialRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class CredencialServiceTest {

    private final CredencialRepository repository = mock(
        CredencialRepository.class
    );

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final CredencialService service = new CredencialService(
        repository,
        passwordEncoder
    );

    @Test
    void deveAutenticarComSenhaCorreta() {
        String senha = "123456";
        String senhaCriptografada = passwordEncoder.encode(senha);

        Credencial credencial = mock(Credencial.class);

        when(credencial.isAtivo()).thenReturn(true);
        when(credencial.getSenha()).thenReturn(senhaCriptografada);

        when(repository.findByUsuario_Email("teste@email.com")).thenReturn(
            java.util.Optional.of(credencial)
        );

        boolean resultado = service.autenticar("teste@email.com", senha);

        assertTrue(resultado);
    }

    @Test
    void naoDeveAutenticarComSenhaIncorreta() {
        String senhaCorreta = "123456";
        String senhaIncorreta = "654321";

        String senhaCriptografada = passwordEncoder.encode(senhaCorreta);

        Credencial credencial = mock(Credencial.class);

        when(credencial.isAtivo()).thenReturn(true);
        when(credencial.getSenha()).thenReturn(senhaCriptografada);

        when(repository.findByUsuario_Email("teste@email.com")).thenReturn(
            java.util.Optional.of(credencial)
        );

        boolean resultado = service.autenticar(
            "teste@email.com",
            senhaIncorreta
        );

        assertFalse(resultado);
    }
}
