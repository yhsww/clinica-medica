package kyj.poo.clinica_medica.service;

import java.util.List;
import kyj.poo.clinica_medica.exception.OperacaoInvalidaException;
import kyj.poo.clinica_medica.exception.RecursoNaoEncontradoException;
import kyj.poo.clinica_medica.model.Conta;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.repository.ContaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContaService {

    private final ContaRepository contaRepository;

    public ContaService(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    public List<Conta> listarTodos() {
        return contaRepository.findAll();
    }

    public List<Conta> listarPorPaciente(Paciente paciente) {
        if (paciente == null) {
            throw new IllegalArgumentException("O paciente é obrigatório.");
        }

        return contaRepository.findByConsulta_Paciente(paciente);
    }

    public List<Conta> listarPorMedico(Medico medico) {
        if (medico == null) {
            throw new IllegalArgumentException("O médico é obrigatório.");
        }

        return contaRepository.findByConsulta_Medico(medico);
    }

    public List<Conta> listarPendentes() {
        return contaRepository.findByPagaFalse();
    }

    public List<Conta> listarPagas() {
        return contaRepository.findByPagaTrue();
    }

    public List<Conta> listarPendentes(Paciente paciente) {
        return listarPorPaciente(paciente)
            .stream()
            .filter(conta -> !conta.isPaga())
            .toList();
    }

    public List<Conta> listarPagas(Paciente paciente) {
        return listarPorPaciente(paciente)
            .stream()
            .filter(Conta::isPaga)
            .toList();
    }

    public Conta buscarPorId(Long contaId) {
        if (contaId == null) {
            throw new IllegalArgumentException("O ID da conta é obrigatório.");
        }

        return contaRepository
            .findById(contaId)
            .orElseThrow(() ->
                new RecursoNaoEncontradoException("Conta não encontrada.")
            );
    }

    @Transactional
    public Conta pagar(Long contaId) {
        Conta conta = buscarPorId(contaId);

        if (conta.isPaga()) {
            throw new OperacaoInvalidaException("Esta conta já foi paga.");
        }

        conta.setPaga(true);

        return contaRepository.save(conta);
    }

    @Transactional
    public void excluir(Long contaId) {
        contaRepository.delete(buscarPorId(contaId));
    }
}
