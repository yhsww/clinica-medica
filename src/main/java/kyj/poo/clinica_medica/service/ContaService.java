package kyj.poo.clinica_medica.service;

import java.math.BigDecimal;
import java.util.List;
import kyj.poo.clinica_medica.exception.OperacaoInvalidaException;
import kyj.poo.clinica_medica.exception.RecursoNaoEncontradoException;
import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.Conta;
import kyj.poo.clinica_medica.model.EstadoDaConsulta;
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

  @Transactional
  public Conta criarConta(Consulta consulta) {

    if (consulta == null) {
      throw new IllegalArgumentException("A consulta é obrigatória.");
    }

    if (consulta.getEstado() != EstadoDaConsulta.REALIZADA) {
      throw new OperacaoInvalidaException(
          "A conta só pode ser criada para uma consulta realizada.");
    }

    if (contaRepository.findByConsulta(consulta).isPresent()) {
      throw new OperacaoInvalidaException("Esta consulta já possui uma conta.");
    }

    BigDecimal valor = consulta.getValorCobrado();

    if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("O valor da conta é inválido.");
    }

    if (valor.compareTo(BigDecimal.ZERO) == 0) {
      return null;
    }

    Conta conta = new Conta(consulta, valor);

    return contaRepository.save(conta);
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

  public Conta buscarPorId(Long contaId) {

    if (contaId == null) {
      throw new IllegalArgumentException("O ID da conta é obrigatório.");
    }

    return contaRepository
        .findById(contaId)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada."));
  }

  public List<Conta> listarPorPaciente(Paciente paciente) {

    if (paciente == null) {
      throw new IllegalArgumentException("O paciente é obrigatório.");
    }

    return contaRepository.findByConsulta_Paciente(paciente);
  }

  public List<Conta> listarPendentes(Paciente paciente) {

    if (paciente == null) {
      throw new IllegalArgumentException("O paciente é obrigatório.");
    }

    return contaRepository.findByConsulta_Paciente(paciente).stream()
        .filter(conta -> !conta.isPaga())
        .toList();
  }
}
