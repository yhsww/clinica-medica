package kyj.poo.clinica_medica.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import kyj.poo.clinica_medica.exception.AgendamentoIndisponivelException;
import kyj.poo.clinica_medica.exception.OperacaoInvalidaException;
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
        ConsultaRepository consultaRepository,
        ListaDeEsperaRepository listaDeEsperaRepository
    ) {
        this.consultaRepository = consultaRepository;
        this.listaDeEsperaRepository = listaDeEsperaRepository;
    }

    @Transactional(noRollbackFor = AgendamentoIndisponivelException.class)
    public Consulta agendar(
        Medico medico,
        Paciente paciente,
        LocalDateTime dataHora
    ) {
        validarDados(medico, paciente, dataHora);

        if (possuiConsultaAgendada(medico, paciente, dataHora, null)) {
            throw new AgendamentoIndisponivelException(
                "O paciente já possui uma consulta agendada com este médico nessa data."
            );
        }

        long consultasAtivas = contarConsultasAgendadas(medico, dataHora, null);
        int limite = medico.getEspecialidade().getLimiteConsultasPorDia();

        if (consultasAtivas >= limite) {
            adicionarNaListaDeEspera(medico, paciente, dataHora);
            throw new AgendamentoIndisponivelException(
                "Não há vagas para este médico nessa data. " +
                    "O paciente foi incluído na lista de espera."
            );
        }

        BigDecimal valor = calcularValorConsulta(medico, paciente);
        Consulta consulta = new Consulta(medico, paciente, dataHora, valor);
        return consultaRepository.save(consulta);
    }

    /**
     * Remarca uma consulta AGENDADA aplicando as mesmas regras do
     * agendamento (data, duplicidade e limite diário), sem contar a própria
     * consulta. Não usa lista de espera: se o dia estiver lotado, recusa.
     */
    @Transactional
    public Consulta remarcar(
        Consulta consulta,
        Medico medico,
        Paciente paciente,
        LocalDateTime dataHora
    ) {
        if (consulta == null) {
            throw new IllegalArgumentException("A consulta é obrigatória.");
        }

        if (consulta.getEstado() != EstadoDaConsulta.AGENDADA) {
            throw new OperacaoInvalidaException(
                "Somente consultas agendadas podem ser alteradas."
            );
        }

        validarDados(medico, paciente, dataHora);

        if (possuiConsultaAgendada(medico, paciente, dataHora, consulta.getId())) {
            throw new AgendamentoIndisponivelException(
                "O paciente já possui uma consulta agendada com este médico nessa data."
            );
        }

        long consultasAtivas = contarConsultasAgendadas(
            medico,
            dataHora,
            consulta.getId()
        );

        if (consultasAtivas >= medico.getEspecialidade().getLimiteConsultasPorDia()) {
            throw new AgendamentoIndisponivelException(
                "Não há vagas para este médico nessa data."
            );
        }

        consulta.setMedico(medico);
        consulta.setPaciente(paciente);
        consulta.setDataHora(dataHora);
        consulta.setValorCobrado(calcularValorConsulta(medico, paciente));

        return consultaRepository.save(consulta);
    }

    private boolean possuiConsultaAgendada(
        Medico medico,
        Paciente paciente,
        LocalDateTime dataHora,
        Long ignorarConsultaId
    ) {
        LocalDate data = dataHora.toLocalDate();

        return consultaRepository
            .findByMedicoAndPacienteAndDataHoraGreaterThanEqualAndDataHoraLessThan(
                medico,
                paciente,
                data.atStartOfDay(),
                data.plusDays(1).atStartOfDay()
            )
            .stream()
            .filter(consulta -> ignorarConsultaId == null || !ignorarConsultaId.equals(consulta.getId()))
            .anyMatch(
                consulta -> consulta.getEstado() == EstadoDaConsulta.AGENDADA
            );
    }

    private long contarConsultasAgendadas(
        Medico medico,
        LocalDateTime dataHora,
        Long ignorarConsultaId
    ) {
        LocalDate data = dataHora.toLocalDate();

        return consultaRepository
            .findByMedicoAndDataHoraGreaterThanEqualAndDataHoraLessThan(
                medico,
                data.atStartOfDay(),
                data.plusDays(1).atStartOfDay()
            )
            .stream()
            .filter(consulta -> ignorarConsultaId == null || !ignorarConsultaId.equals(consulta.getId()))
            .filter(
                consulta -> consulta.getEstado() == EstadoDaConsulta.AGENDADA
            )
            .count();
    }

    private void adicionarNaListaDeEspera(
        Medico medico,
        Paciente paciente,
        LocalDateTime dataHora
    ) {
        LocalDate data = dataHora.toLocalDate();
        List<ListaDeEspera> fila =
            listaDeEsperaRepository.findByMedicoAndDataHoraGreaterThanEqualAndDataHoraLessThanOrderByPosicaoAsc(
                medico,
                data.atStartOfDay(),
                data.plusDays(1).atStartOfDay()
            );

        boolean jaEstaNaFila = fila
            .stream()
            .anyMatch(entrada -> entrada.getPaciente().equals(paciente));

        if (jaEstaNaFila) {
            throw new AgendamentoIndisponivelException(
                "O paciente já está na lista de espera para este médico nessa data."
            );
        }

        int posicao = fila.size() + 1;
        ListaDeEspera entrada = new ListaDeEspera(
            medico,
            paciente,
            dataHora,
            posicao
        );
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

    private void validarDados(
        Medico medico,
        Paciente paciente,
        LocalDateTime dataHora
    ) {
        if (medico == null) {
            throw new IllegalArgumentException("O médico é obrigatório.");
        }
        if (paciente == null) {
            throw new IllegalArgumentException("O paciente é obrigatório.");
        }
        if (dataHora == null) {
            throw new IllegalArgumentException(
                "A data da consulta é obrigatória."
            );
        }
        if (dataHora.toLocalDate().isBefore(LocalDate.now())) {
            throw new AgendamentoIndisponivelException(
                "Não é possível agendar uma consulta para uma data passada."
            );
        }
        if (medico.getEspecialidade() == null) {
            throw new IllegalArgumentException(
                "O médico precisa possuir uma especialidade."
            );
        }
        if (
            medico.getValorConsultaParticular() == null ||
            medico.getValorConsultaParticular().compareTo(BigDecimal.ZERO) < 0
        ) {
            throw new IllegalArgumentException(
                "O valor da consulta particular deve ser válido."
            );
        }
    }
}
