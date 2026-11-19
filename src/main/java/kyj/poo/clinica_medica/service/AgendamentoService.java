package kyj.poo.clinica_medica.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import kyj.poo.clinica_medica.exception.AgendamentoIndisponivelException;
import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.EstadoDaConsulta;
import kyj.poo.clinica_medica.model.ListaDeEspera;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.model.Plano;
import kyj.poo.clinica_medica.repository.ConsultaRepository;
import kyj.poo.clinica_medica.repository.ListaDeEsperaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgendamentoService {

  private final ConsultaRepository consultaRepository;
  private final ListaDeEsperaRepository listaDeEsperaRepository;

  public AgendamentoService(
      ConsultaRepository consultaRepository, ListaDeEsperaRepository listaDeEsperaRepository) {

    this.consultaRepository = consultaRepository;
    this.listaDeEsperaRepository = listaDeEsperaRepository;
  }

  @Transactional
  public Consulta agendar(Medico medico, Paciente paciente, LocalDate data) {

    validarDados(medico, paciente, data);

    boolean jaAgendado =
        consultaRepository
            .findByMedicoAndPacienteAndData(medico, paciente, data)
            .filter(consulta -> consulta.getEstado() == EstadoDaConsulta.AGENDADA)
            .isPresent();

    if (jaAgendado) {
      throw new AgendamentoIndisponivelException(
          "O paciente já possui uma consulta agendada " + "com este médico nessa data.");
    }

    List<Consulta> consultasDoDia = consultaRepository.findByMedicoAndData(medico, data);

    long consultasAtivas =
        consultasDoDia.stream()
            .filter(consulta -> consulta.getEstado() == EstadoDaConsulta.AGENDADA)
            .count();

    int limite = medico.getEspecialidade().getLimiteConsultasPorDia();

    if (consultasAtivas >= limite) {

      adicionarNaListaDeEspera(medico, paciente, data);

      throw new AgendamentoIndisponivelException(
          "Não há vagas para este médico nessa data. "
              + "O paciente foi incluído na lista de espera.");
    }

    BigDecimal valor = calcularValorConsulta(medico, paciente);

    Consulta consulta = new Consulta(medico, paciente, data, valor);

    return consultaRepository.save(consulta);
  }

  private void adicionarNaListaDeEspera(Medico medico, Paciente paciente, LocalDate data) {

    List<ListaDeEspera> fila =
        listaDeEsperaRepository.findByMedicoAndDataOrderByPosicaoAsc(medico, data);

    boolean jaEstaNaFila =
        fila.stream().anyMatch(entrada -> entrada.getPaciente().equals(paciente));

    if (jaEstaNaFila) {
      throw new AgendamentoIndisponivelException(
          "O paciente já está na lista de espera " + "para este médico nessa data.");
    }

    int posicao = fila.size() + 1;

    ListaDeEspera entrada = new ListaDeEspera(medico, paciente, data, posicao);

    listaDeEsperaRepository.save(entrada);
  }

  private BigDecimal calcularValorConsulta(Medico medico, Paciente paciente) {

    Plano plano = paciente.getPlanoSaude();

    if (plano != null && medico.aceitaPlano(plano)) {
      return BigDecimal.ZERO;
    }

    BigDecimal valorBase = medico.getValorConsultaParticular();

    return medico.getEspecialidade().calcularValorConsulta(valorBase);
  }

  private void validarDados(Medico medico, Paciente paciente, LocalDate data) {

    if (medico == null) {
      throw new IllegalArgumentException("O médico é obrigatório.");
    }

    if (paciente == null) {
      throw new IllegalArgumentException("O paciente é obrigatório.");
    }

    if (data == null) {
      throw new IllegalArgumentException("A data da consulta é obrigatória.");
    }

    if (data.isBefore(LocalDate.now())) {
      throw new AgendamentoIndisponivelException(
          "Não é possível agendar uma consulta para uma data passada.");
    }

    if (medico.getEspecialidade() == null) {
      throw new IllegalArgumentException("O médico precisa possuir uma especialidade.");
    }

    if (medico.getValorConsultaParticular() == null
        || medico.getValorConsultaParticular().compareTo(BigDecimal.ZERO) < 0) {

      throw new IllegalArgumentException("O valor da consulta particular deve ser válido.");
    }
  }
}
