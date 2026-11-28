package kyj.poo.clinica_medica.service;

import java.util.List;
import kyj.poo.clinica_medica.dto.ConsultaRequest;
import kyj.poo.clinica_medica.exception.AgendamentoIndisponivelException;
import kyj.poo.clinica_medica.exception.OperacaoInvalidaException;
import kyj.poo.clinica_medica.exception.RecursoNaoEncontradoException;
import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.Conta;
import kyj.poo.clinica_medica.model.EstadoDaConsulta;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.repository.ConsultaRepository;
import kyj.poo.clinica_medica.repository.ContaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final ListaDeEsperaService listaDeEsperaService;
    private final MedicoService medicoService;
    private final PacienteService pacienteService;
    private final AgendamentoService agendamentoService;
    private final ContaRepository contaRepository;

    public ConsultaService(
        ConsultaRepository consultaRepository,
        ListaDeEsperaService listaDeEsperaService,
        MedicoService medicoService,
        PacienteService pacienteService,
        AgendamentoService agendamentoService,
        ContaRepository contaRepository
    ) {
        this.consultaRepository = consultaRepository;
        this.listaDeEsperaService = listaDeEsperaService;
        this.medicoService = medicoService;
        this.pacienteService = pacienteService;
        this.agendamentoService = agendamentoService;
        this.contaRepository = contaRepository;
    }

    public List<Consulta> listarTodos() {
        return consultaRepository.findAll();
    }

    public List<Consulta> listarPorPaciente(Paciente paciente) {
        if (paciente == null) {
            throw new IllegalArgumentException("O paciente é obrigatório.");
        }

        return consultaRepository.findByPacienteOrderByDataHoraDesc(paciente);
    }

    public List<Consulta> listarPorMedico(Medico medico) {
        if (medico == null) {
            throw new IllegalArgumentException("O médico é obrigatório.");
        }

        return consultaRepository.findByMedicoOrderByDataHoraDesc(medico);
    }

    @Transactional(noRollbackFor = AgendamentoIndisponivelException.class)
    public Consulta criar(ConsultaRequest request) {
        validarRequest(request);

        Medico medico = medicoService.buscarPorId(request.medicoId());
        Paciente paciente = pacienteService.buscarPorId(request.pacienteId());

        Consulta consulta = agendamentoService.agendar(
            medico,
            paciente,
            request.dataHora()
        );
        criarConta(consulta);

        return consulta;
    }

    /**
     * Gera a conta apenas quando há valor a pagar (paciente sem plano aceito
     * pelo médico). Pacientes cobertos pelo plano não pagam (§7).
     */
    private void criarConta(Consulta consulta) {
        if (
            consulta == null ||
            consulta.getValorCobrado() == null ||
            consulta.getValorCobrado().signum() <= 0
        ) {
            return;
        }

        Conta conta = new Conta(consulta, consulta.getValorCobrado());
        contaRepository.save(conta);
    }

    @Transactional
    public Consulta atualizar(Long id, ConsultaRequest request) {
        validarRequest(request);

        Consulta consulta = buscarPorId(id);
        consulta.setMedico(medicoService.buscarPorId(request.medicoId()));
        consulta.setPaciente(pacienteService.buscarPorId(request.pacienteId()));
        consulta.setDataHora(request.dataHora());

        return consultaRepository.save(consulta);
    }

    @Transactional
    public void excluir(Long id) {
        Consulta consulta = buscarPorId(id);

        contaRepository
            .findByConsulta(consulta)
            .ifPresent(contaRepository::delete);
        consultaRepository.delete(consulta);
    }

    /**
     * Tudo numa única transação: se qualquer passo falhar, nada é gravado
     * (cancelamento, remoção da conta, promoção da fila e nova conta).
     */
    @Transactional
    public Consulta cancelar(Long consultaId) {
        Consulta consulta = buscarPorId(consultaId);

        if (consulta.getEstado() != EstadoDaConsulta.AGENDADA) {
            throw new OperacaoInvalidaException(
                "Somente consultas agendadas podem ser canceladas."
            );
        }

        consulta.setEstado(EstadoDaConsulta.CANCELADA);

        Consulta consultaCancelada = consultaRepository.save(consulta);

        contaRepository
            .findByConsulta(consulta)
            .filter(conta -> !conta.isPaga())
            .ifPresent(contaRepository::delete);

        Consulta promovida = listaDeEsperaService.promoverPrimeiroDaFila(
            consulta.getMedico(),
            consulta.getDataHora().toLocalDate()
        );
        criarConta(promovida);

        return consultaCancelada;
    }

    @Transactional
    public Consulta realizar(Long consultaId) {
        Consulta consulta = buscarPorId(consultaId);

        if (consulta.getEstado() != EstadoDaConsulta.AGENDADA) {
            throw new OperacaoInvalidaException(
                "Somente consultas agendadas podem ser realizadas."
            );
        }

        consulta.setEstado(EstadoDaConsulta.REALIZADA);

        return consultaRepository.save(consulta);
    }

    public Consulta buscarPorId(Long consultaId) {
        if (consultaId == null) {
            throw new IllegalArgumentException(
                "O ID da consulta é obrigatório."
            );
        }

        return consultaRepository
            .findById(consultaId)
            .orElseThrow(() ->
                new RecursoNaoEncontradoException("Consulta não encontrada.")
            );
    }

    public boolean pertenceAoPaciente(Consulta consulta, Paciente paciente) {
        if (consulta == null || paciente == null) {
            return false;
        }

        return consulta.getPaciente().equals(paciente);
    }

    public boolean pertenceAoMedico(Consulta consulta, Medico medico) {
        if (consulta == null || medico == null) {
            return false;
        }

        return consulta.getMedico().equals(medico);
    }

    /** Médico "autorizado a atender" o paciente = já tem consulta com ele. */
    public boolean medicoAtendePaciente(Medico medico, Paciente paciente) {
        if (medico == null || paciente == null) {
            return false;
        }

        return consultaRepository.existsByMedicoAndPaciente(medico, paciente);
    }

    private void validarRequest(ConsultaRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                "Os dados da consulta são obrigatórios."
            );
        }

        if (request.medicoId() == null) {
            throw new IllegalArgumentException("O ID do médico é obrigatório.");
        }

        if (request.pacienteId() == null) {
            throw new IllegalArgumentException(
                "O ID do paciente é obrigatório."
            );
        }

        if (request.dataHora() == null) {
            throw new IllegalArgumentException(
                "A data da consulta é obrigatória."
            );
        }
    }
}
