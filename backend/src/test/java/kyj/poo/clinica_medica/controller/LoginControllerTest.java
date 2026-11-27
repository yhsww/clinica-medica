package kyj.poo.clinica_medica.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import kyj.poo.clinica_medica.dto.CadastroRequest;
import kyj.poo.clinica_medica.model.Credencial;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.model.Papel;
import kyj.poo.clinica_medica.model.PlanoNome;
import kyj.poo.clinica_medica.security.JwtService;
import kyj.poo.clinica_medica.service.AuthService;
import org.junit.jupiter.api.Test;

class LoginControllerTest {

    private final AuthService authService = mock(AuthService.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final LoginController controlador = new LoginController(
        authService,
        jwtService
    );

    @Test
    void deveIncluirTokenNaRespostaDoLogin() {
        Credencial credencial = credencial();
        when(authService.autenticar("ana@clinica.com", "senha123")).thenReturn(
            credencial
        );
        when(jwtService.gerarToken(credencial)).thenReturn("token-jwt");

        var resposta = controlador.login(
            new LoginController.LoginRequest("ana@clinica.com", "senha123")
        );

        assertEquals(200, resposta.getStatusCode().value());
        assertEquals("token-jwt", resposta.getBody().token());
        assertEquals("PACIENTE", resposta.getBody().papel());
    }

    @Test
    void deveIncluirTokenNaRespostaDoCadastro() {
        CadastroRequest request = new CadastroRequest(
            "Ana",
            "ana@clinica.com",
            "senha123",
            "PACIENTE",
            30,
            PlanoNome.SUS,
            null,
            null
        );
        Credencial credencial = credencial();
        when(authService.cadastrar(request)).thenReturn(credencial);
        when(jwtService.gerarToken(credencial)).thenReturn("token-jwt");

        var resposta = controlador.cadastrar(request);

        assertEquals(201, resposta.getStatusCode().value());
        assertEquals("token-jwt", resposta.getBody().token());
    }

    private Credencial credencial() {
        Paciente paciente = new Paciente("Ana", "ana@clinica.com", 30, null);
        return new Credencial(paciente, "senha123", Papel.PACIENTE);
    }
}
