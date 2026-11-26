package kyj.poo.clinica_medica.service;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgendamentoService {

    private final ConsultaRepository consultaRepository;
    private final ListaDeEsperaRepository listaDeEsperaRepository;

    public AgendamentoService(
            ConsultaRepository consultaRepository,
            ListaDeEsperaRepository listaDeEsperaRepository) {

        this.consultaRepository = consultaRepository;
        this.listaDeEsperaRepository = listaDeEsperaRepository;
    }

    @Transactional(noRollbackFor = AgendamentoIndisponivelException.class)
    public Consulta agendar(Medico medico, Paciente paciente, LocalDateTime dataHora) {

        validarDados(medico, paciente, dataHora);

        boolean jaAgendado =
                consultaRepository
                        .findByMedicoAndPacienteAndDataHoraGreaterThanEqualAndDataHoraLessThan(\n                                medico, dataHora.toLocalDate().atStartOfDay(), dataHora.toLocalDate().plusDays(1).atStartOfDay())
                        .filter(consulta -> consulta.getEstado() == EstadoDaConsulta.AGENDADA)
                        .isPresent();

        if (jaAgendado) {
            throw new AgendamentoIndisponivelException(
                    "O paciente já possui uma consulta agendada " + "com este médico nessa data.");
        }

        List<Consulta> consultasDoDia = consultaRepository.findByMedicoAndDataHoraGreaterThanEqualAndDataHoraLessThan(\n                medico, dataHora.toLocalDate().atStartOfDay(), dataHora.toLocalDate().plusDays(1).atStartOfDay());

        long consultasAtivas =
                consultasDoDia.stream()
                        .filter(consulta -> consulta.getEstado() == EstadoDaConsulta.AGENDADA)
                        .count();

        int limite = medico.getEspecialidade().getLimiteConsultasPorDia();

        if (consultasAtivas >= limite) {

            adicionarNaListaDeEspera(medico, paciente, dataHora);

            throw new AgendamentoIndisponivelException(
                    "Não há vagas para este médico nessa data. "
                            + "O paciente foi incluído na lista de espera.");
        }

        BigDecimal valor = calcularValorConsulta(medico, paciente);

        Consulta consulta = new Consulta(medico, paciente, dataHora, valor);

        return consultaRepository.save(consulta);
    }

    private void adicionarNaListaDeEspera(Medico medico, Paciente paciente, LocalDateTime dataHora) {

        List<ListaDeEspera> fila =
                listaDeEsperaRepository.findByMedicoAndDataHoraGreaterThanEqualAndDataHoraLessThanOrderByPosicaoAsc(\n                        medico, dataHora.toLocalDate().atStartOfDay(), dataHora.toLocalDate().plusDays(1).atStartOfDay());

        boolean jaEstaNaFila =
                fila.stream().anyMatch(entrada -> entrada.getPaciente().equals(paciente));

        if (jaEstaNaFila) {
            throw new AgendamentoIndisponivelException(
                    "O paciente já está na lista de espera " + "para este médico nessa data.");
        }

        int posicao = fila.size() + 1;

        ListaDeEspera entrada = new ListaDeEspera(medico, paciente, dataHora, posicao);

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

    private void validarDados(Medico medico, Paciente paciente, LocalDateTime dataHora) {

        if (medico == null) {
            throw new IllegalArgumentException("O médico é obrigatório.");
        }

        if (paciente == null) {
            throw new IllegalArgumentException("O paciente é obrigatório.");
        }

        if (dataHora == null) {
            throw new IllegalArgumentException("A data da consulta é obrigatória.");
        }

        if (dataHora.toLocalDate().isBefore(LocalDate.now())) {
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
