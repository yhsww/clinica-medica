package kyj.poo.clinica_medica.controller;

import jakarta.validation.Valid;
import kyj.poo.clinica_medica.dto.CadastroRequest;
import kyj.poo.clinica_medica.model.Credencial;
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

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    public record LoginRequest(String email, String senha) {}

    public record LoginResponse(
        Long id,
        String nome,
        String email,
        String papel
    ) {}

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        Credencial credencial = authService.autenticar(
            request.email(),
            request.senha()
        );
        var usuario = credencial.getUsuario();
        return ResponseEntity.ok(
            new LoginResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                credencial.getPapel().name()
            )
        );
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<LoginResponse> cadastrar(
        @Valid @RequestBody CadastroRequest request
    ) {
        Credencial credencial = authService.cadastrar(request);
        var usuario = credencial.getUsuario();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            new LoginResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                credencial.getPapel().name()
            )
        );
    }
}
