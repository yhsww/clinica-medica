package kyj.poo.clinica_medica.controller;

import jakarta.validation.Valid;
import kyj.poo.clinica_medica.dto.CadastroRequest;
import kyj.poo.clinica_medica.model.Credencial;
import kyj.poo.clinica_medica.security.JwtService;
import kyj.poo.clinica_medica.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final AuthService authService;
    private final JwtService jwtService;

    public LoginController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    public record LoginRequest(String email, String senha) {}

    public record LoginResponse(
        Long id,
        String nome,
        String email,
        String papel,
        String token
    ) {}

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        Credencial credencial = authService.autenticar(
            request.email(),
            request.senha()
        );
        return ResponseEntity.ok(resposta(credencial));
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<LoginResponse> cadastrar(
        @Valid @RequestBody CadastroRequest request
    ) {
        Credencial credencial = authService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            resposta(credencial)
        );
    }

    private LoginResponse resposta(Credencial credencial) {
        var usuario = credencial.getUsuario();
        return new LoginResponse(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            credencial.getPapel().name(),
            jwtService.gerarToken(credencial)
        );
    }
}
