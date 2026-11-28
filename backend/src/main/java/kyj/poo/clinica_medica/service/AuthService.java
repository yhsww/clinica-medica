package kyj.poo.clinica_medica.service;

import java.util.List;
import kyj.poo.clinica_medica.dto.CadastroRequest;
import kyj.poo.clinica_medica.dto.MedicoRequest;
import kyj.poo.clinica_medica.dto.PacienteRequest;
import kyj.poo.clinica_medica.model.Credencial;
import kyj.poo.clinica_medica.model.Papel;
import kyj.poo.clinica_medica.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final CredencialService credencialService;
    private final PacienteService pacienteService;
    private final MedicoService medicoService;

    public AuthService(
        CredencialService credencialService,
        PacienteService pacienteService,
        MedicoService medicoService
    ) {
        this.credencialService = credencialService;
        this.pacienteService = pacienteService;
        this.medicoService = medicoService;
    }

    public Credencial autenticar(String email, String senha) {
        if (!credencialService.autenticar(email, senha)) {
            throw new IllegalArgumentException("E-mail ou senha inválidos.");
        }

        return credencialService.buscarPorEmail(email);
    }

    @Transactional
    public Credencial cadastrar(CadastroRequest request) {
        boolean medico = "MEDICO".equalsIgnoreCase(request.tipo());
        boolean paciente = "PACIENTE".equalsIgnoreCase(request.tipo());

        if (!medico && !paciente) {
            throw new IllegalArgumentException(
                "Informe o tipo de cadastro: PACIENTE ou MÉDICO."
            );
        }

        Usuario usuario;
        Papel papel;

        if (medico) {
            if (request.especialidade() == null) {
                throw new IllegalArgumentException(
                    "A especialidade é obrigatória."
                );
            }

            if (request.valorConsultaParticular() == null) {
                throw new IllegalArgumentException(
                    "O valor da consulta é obrigatório."
                );
            }

            usuario = medicoService.criar(
                new MedicoRequest(
                    request.nome(),
                    request.email(),
                    request.especialidade(),
                    request.valorConsultaParticular(),
                    List.of()
                )
            );
            papel = Papel.MEDICO;
        } else {
            if (request.idade() == null || request.idade() <= 0) {
                throw new IllegalArgumentException(
                    "A idade deve ser maior que zero."
                );
            }

            usuario = pacienteService.criar(
                new PacienteRequest(
                    request.nome(),
                    request.email(),
                    request.idade(),
                    request.planoSaude()
                )
            );
            papel = Papel.PACIENTE;
        }

        return credencialService.criar(usuario, request.senha(), papel);
    }
}
