package kyj.poo.clinica_medica.service;

import kyj.poo.clinica_medica.model.Credencial;
import kyj.poo.clinica_medica.model.Papel;
import kyj.poo.clinica_medica.model.Usuario;
import kyj.poo.clinica_medica.repository.CredencialRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CredencialService {

    private final CredencialRepository repository;
    private final PasswordEncoder passwordEncoder;

    public CredencialService(
            CredencialRepository repository,
            PasswordEncoder passwordEncoder) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Credencial criar(Usuario usuario, String senha, Papel papel) {

        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo.");
        }

        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new IllegalArgumentException("E-mail do usuário não pode ser vazio.");
        }

        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Senha não pode ser vazia.");
        }

        if (papel == null) {
            throw new IllegalArgumentException("Papel não pode ser nulo.");
        }

        if (repository.existsByUsuario_Email(usuario.getEmail())) {
            throw new IllegalArgumentException("Já existe uma credencial para este usuário.");
        }

        String senhaCriptografada = passwordEncoder.encode(senha);

        Credencial credencial =
                new Credencial(usuario, senhaCriptografada, papel);

        return repository.save(credencial);
    }

    public Credencial buscarPorEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail não pode ser vazio.");
        }

        return repository
                .findByUsuario_Email(email)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Credencial não encontrada para o e-mail informado."));
    }

    public boolean autenticar(String email, String senha) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail não pode ser vazio.");
        }

        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Senha não pode ser vazia.");
        }

        Credencial credencial = buscarPorEmail(email);

        if (!credencial.isAtivo()) {
            return false;
        }

        return passwordEncoder.matches(senha, credencial.getSenha());
    }
}