package kyj.poo.clinica_medica.service;

import kyj.poo.clinica_medica.exception.RecursoNaoEncontradoException;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.model.Plano;
import kyj.poo.clinica_medica.repository.PacienteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
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

    @Transactional
    public Paciente atualizar(Paciente paciente) {

        if (paciente == null || paciente.getId() == null) {
            throw new IllegalArgumentException("Paciente inválido para atualização.");
        }

        Paciente existente = buscarPorId(paciente.getId());

        if (paciente.getNome() == null || paciente.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do paciente é obrigatório.");
        }

        if (paciente.getEmail() == null || paciente.getEmail().isBlank()) {
            throw new IllegalArgumentException("O e-mail do paciente é obrigatório.");
        }

        if (paciente.getIdade() == null || paciente.getIdade() <= 0) {
            throw new IllegalArgumentException("A idade do paciente deve ser válida.");
        }

        existente.setNome(paciente.getNome());
        existente.setEmail(paciente.getEmail());
        existente.setIdade(paciente.getIdade());
        existente.setPlanoSaude(paciente.getPlanoSaude());

        return pacienteRepository.save(existente);
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