package kyj.poo.clinica_medica.service;

import kyj.poo.clinica_medica.dto.PacienteRequest;
import kyj.poo.clinica_medica.exception.OperacaoInvalidaException;
import kyj.poo.clinica_medica.exception.RecursoNaoEncontradoException;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.model.Plano;
import kyj.poo.clinica_medica.model.PlanoNome;
import kyj.poo.clinica_medica.repository.ConsultaRepository;
import kyj.poo.clinica_medica.repository.CredencialRepository;
import kyj.poo.clinica_medica.repository.PacienteRepository;
import kyj.poo.clinica_medica.repository.PlanoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PlanoRepository planoRepository;
    private final CredencialRepository credencialRepository;
    private final ConsultaRepository consultaRepository;

    public PacienteService(
            PacienteRepository pacienteRepository,
            PlanoRepository planoRepository,
            CredencialRepository credencialRepository,
            ConsultaRepository consultaRepository) {

        this.pacienteRepository = pacienteRepository;
        this.planoRepository = planoRepository;
        this.credencialRepository = credencialRepository;
        this.consultaRepository = consultaRepository;
    }

    @Transactional
    public Paciente criar(PacienteRequest request) {
        Paciente paciente = new Paciente();
        aplicar(paciente, request);
        return salvar(paciente);
    }

    @Transactional
    public Paciente atualizar(Long id, PacienteRequest request) {
        Paciente paciente = buscarPorId(id);
        aplicar(paciente, request);
        return salvar(paciente);
    }

    @Transactional
    public void excluir(Long id) {
        Paciente paciente = buscarPorId(id);

        if (consultaRepository.existsByPaciente_Id(id)) {
            throw new OperacaoInvalidaException(
                    "Não é possível excluir o paciente: existem consultas associadas.");
        }

        credencialRepository.findByUsuario_Id(id).ifPresent(credencialRepository::delete);
        pacienteRepository.delete(paciente);
    }

    private void aplicar(Paciente paciente, PacienteRequest request) {
        paciente.setNome(request.nome().trim());
        paciente.setEmail(request.email().trim());
        paciente.setIdade(request.idade());
        PlanoNome plano = request.planoSaude();
        paciente.setPlanoSaude(plano == null ? null : resolverPlano(plano.getNome()));
    }

    private Plano resolverPlano(String nome) {

        if (nome == null || nome.isBlank()) {
            return null;
        }

        String nomeLimpo = nome.trim();

        return planoRepository
                .findByNomeIgnoreCase(nomeLimpo)
                .orElseThrow(() -> new OperacaoInvalidaException("Plano inválido: " + nome));
    }

    @Transactional
    public Paciente salvar(Paciente paciente) {

        validarPaciente(paciente);

        return pacienteRepository.save(paciente);
    }

    public Paciente buscarPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("O ID do paciente é obrigatório.");
        }

        return pacienteRepository
                .findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado."));
    }

    public Paciente buscarPorEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("O e-mail é obrigatório.");
        }

        return pacienteRepository
                .findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado."));
    }

    public List<Paciente> listarTodos() {
        return pacienteRepository.findAll();
    }

    public List<Paciente> listarPorPlano(Plano plano) {

        if (plano == null) {
            throw new IllegalArgumentException("O plano é obrigatório.");
        }

        return pacienteRepository.findByPlanoSaude(plano);
    }

    private void validarPaciente(Paciente paciente) {

        if (paciente == null) {
            throw new IllegalArgumentException("O paciente é obrigatório.");
        }

        if (paciente.getNome() == null || paciente.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do paciente é obrigatório.");
        }

        if (paciente.getEmail() == null || paciente.getEmail().isBlank()) {
            throw new IllegalArgumentException("O e-mail do paciente é obrigatório.");
        }

        if (paciente.getIdade() == null || paciente.getIdade() <= 0) {
            throw new IllegalArgumentException("A idade do paciente deve ser válida.");
        }
    }
}
