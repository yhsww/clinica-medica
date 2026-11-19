package kyj.poo.clinica_medica.service;

import kyj.poo.clinica_medica.model.Credencial;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final CredencialService credencialService;

    public AuthService(CredencialService credencialService) {
        this.credencialService = credencialService;
    }

    public Credencial autenticar(String email, String senha) {
        if (!credencialService.autenticar(email, senha)) {
            throw new IllegalArgumentException("E-mail ou senha inválidos.");
        }

        return credencialService.buscarPorEmail(email);
    }
}