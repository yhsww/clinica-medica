package kyj.poo.clinica_medica.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import kyj.poo.clinica_medica.exception.OperacaoInvalidaException;
import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.EstadoDaConsulta;
import kyj.poo.clinica_medica.model.ListaDeEspera;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Notificacao;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.model.Plano;
import kyj.poo.clinica_medica.repository.ConsultaRepository;
import kyj.poo.clinica_medica.repository.ListaDeEsperaRepository;
import kyj.poo.clinica_medica.repository.NotificacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListaDeEsperaService {

    private final ListaDeEsperaRepository listaDeEsperaRepository;
    private final ConsultaRepository consultaRepository;
    private final NotificacaoRepository notificacaoRepository;

    public ListaDeEsperaService(
        ListaDeEsperaRepository listaDeEsperaRepository,
        ConsultaRepository consultaRepository,
        NotificacaoRepository notificacaoRepository
    ) {
        this.listaDeEsperaRepository = listaDeEsperaRepository;
        this.consultaRepository = consultaRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    public ListaDeEspera adicionar(
        Medico medico,
        Paciente paciente,
        LocalDateTime dataHora
    ) {
        validarDados(medico, paciente, dataHora);

        LocalDate data = dataHora.toLocalDate();
        List<ListaDeEspera> fila = buscarFilaDoDia(medico, data);

        boolean jaEstaNaFila = fila
            .stream()
            .anyMatch(entrada -> entrada.getPaciente().equals(paciente));

        if (jaEstaNaFila) {
            throw new OperacaoInvalidaException(
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
        return listaDeEsperaRepository.save(entrada);
    }

    public List<ListaDeEspera> listar(Medico medico, LocalDate data) {
        if (medico == null) {
            throw new IllegalArgumentException("O médico é obrigatório.");
        }
        if (data == null) {
            throw new IllegalArgumentException("A data é obrigatória.");
        }
        return buscarFilaDoDia(medico, data);
    }

    /** Todas as entradas do paciente em listas de espera, por data. */
    public List<ListaDeEspera> listarPorPaciente(Paciente paciente) {
        if (paciente == null) {
            throw new IllegalArgumentException("O paciente é obrigatório.");
        }
        return listaDeEsperaRepository.findByPacienteOrderByDataHoraAsc(
            paciente
        );
    }

    public ListaDeEspera primeiroDaFila(Medico medico, LocalDate data) {
        List<ListaDeEspera> fila = listar(medico, data);
        if (fila.isEmpty()) {
            return null;
        }
        return fila.get(0);
    }

    @Transactional
    public Consulta promoverPrimeiroDaFila(Medico medico, LocalDate data) {
        if (medico == null) {
            throw new IllegalArgumentException("O médico é obrigatório.");
        }
        if (data == null) {
            throw new IllegalArgumentException(
                "A data da consulta é obrigatória."
            );
        }
        if (medico.getEspecialidade() == null) {
            throw new IllegalArgumentException(
                "O médico precisa possuir uma especialidade."
            );
        }

        while (true) {
            ListaDeEspera entrada = primeiroDaFila(medico, data);
            if (entrada == null) {
                return null;
            }

            LocalDateTime inicio = data.atStartOfDay();
            LocalDateTime fim = data.plusDays(1).atStartOfDay();

            long consultasAtivas = consultaRepository
                .findByMedicoAndDataHoraGreaterThanEqualAndDataHoraLessThan(
                    medico,
                    inicio,
                    fim
                )
                .stream()
                .filter(
                    consulta ->
                        consulta.getEstado() == EstadoDaConsulta.AGENDADA
                )
                .count();

            if (
                consultasAtivas >=
                medico.getEspecialidade().getLimiteConsultasPorDia()
            ) {
                return null;
            }

            Paciente paciente = entrada.getPaciente();
            boolean jaPossuiConsulta = consultaRepository
                .findByMedicoAndPacienteAndDataHoraGreaterThanEqualAndDataHoraLessThan(
                    medico,
                    paciente,
                    inicio,
                    fim
                )
                .filter(
                    consulta ->
                        consulta.getEstado() == EstadoDaConsulta.AGENDADA
                )
                .isPresent();

            if (jaPossuiConsulta) {
                listaDeEsperaRepository.delete(entrada);
                reorganizarFila(medico, data);
                continue;
            }

            BigDecimal valor = calcularValorConsulta(medico, paciente);
            Consulta consulta = new Consulta(
                medico,
                paciente,
                entrada.getDataHora(),
                valor
            );
            Consulta consultaSalva = consultaRepository.save(consulta);

            Notificacao notificacao = new Notificacao(
                paciente,
                "Você foi promovido da lista de espera. " +
                    "Sua consulta com o médico " +
                    medico.getNome() +
                    " foi agendada para " +
                    entrada.getDataHora() +
                    "."
            );
            notificacaoRepository.save(notificacao);

            listaDeEsperaRepository.delete(entrada);
            reorganizarFila(medico, data);
            return consultaSalva;
        }
    }

    private List<ListaDeEspera> buscarFilaDoDia(Medico medico, LocalDate data) {
        return listaDeEsperaRepository.findByMedicoAndDataHoraGreaterThanEqualAndDataHoraLessThanOrderByPosicaoAsc(
            medico,
            data.atStartOfDay(),
            data.plusDays(1).atStartOfDay()
        );
    }

    private BigDecimal calcularValorConsulta(Medico medico, Paciente paciente) {
        Plano plano = paciente.getPlanoSaude();
        if (plano != null && medico.aceitaPlano(plano)) {
            return BigDecimal.ZERO;
        }
        BigDecimal valorBase = medico.getValorConsultaParticular();
        return medico.getEspecialidade().calcularValorConsulta(valorBase);
    }

    private void reorganizarFila(Medico medico, LocalDate data) {
        List<ListaDeEspera> fila = buscarFilaDoDia(medico, data);
        for (int i = 0; i < fila.size(); i++) {
            fila.get(i).setPosicao(i + 1);
        }
        listaDeEsperaRepository.saveAll(fila);
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
        if (paciente.getId() == null) {
            throw new IllegalArgumentException(
                "O paciente informado é inválido."
            );
        }
        if (dataHora == null) {
            throw new IllegalArgumentException(
                "A data da consulta é obrigatória."
            );
        }
    }
}
