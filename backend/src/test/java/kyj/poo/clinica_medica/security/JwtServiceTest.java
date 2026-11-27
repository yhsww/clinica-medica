package kyj.poo.clinica_medica.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.jsonwebtoken.JwtException;
import kyj.poo.clinica_medica.model.Credencial;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.model.Papel;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SEGREDO =
        "chave-secreta-para-testes-com-mais-de-32-caracteres";

    private final JwtService servico = new JwtService(SEGREDO, 60_000);

    @Test
    void deveGerarTokenComEmailDoUsuario() {
        Paciente paciente = new Paciente("Ana", "ana@clinica.com", 30, null);
        Credencial credencial = new Credencial(
            paciente,
            "senha123",
            Papel.PACIENTE
        );

        String token = servico.gerarToken(credencial);

        assertNotNull(token);
        assertEquals("ana@clinica.com", servico.extrairEmail(token));
    }

    @Test
    void deveRejeitarTokenExpirado() {
        JwtService servicoExpirado = new JwtService(SEGREDO, -1_000);
        Paciente paciente = new Paciente("Ana", "ana@clinica.com", 30, null);
        Credencial credencial = new Credencial(
            paciente,
            "senha123",
            Papel.PACIENTE
        );

        String token = servicoExpirado.gerarToken(credencial);

        assertThrows(JwtException.class, () ->
            servicoExpirado.extrairEmail(token)
        );
    }
}
