package kyj.poo.clinica_medica.service;

import kyj.poo.clinica_medica.exception.OperacaoInvalidaException;
import kyj.poo.clinica_medica.model.Consulta;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ListaDeEsperaService {

    private final ListaDeEsperaRepository listaDeEsperaRepository;
    private final ConsultaRepository consultaRepository;
    private final NotificacaoRepository notificacaoRepository;

    public ListaDeEsperaService(
            ListaDeEsperaRepository listaDeEsperaRepository,
            ConsultaRepository consultaRepository,
            NotificacaoRepository notificacaoRepository) {

        this.listaDeEsperaRepository = listaDeEsperaRepository;
        this.consultaRepository = consultaRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    public ListaDeEspera adicionar(Medico medico, Paciente paciente, LocalDate data) {

        validarDados(medico, paciente, data);

        List<ListaDeEspera> fila =
                listaDeEsperaRepository.findByMedicoAndDataOrderByPosicaoAsc(medico, data);

        boolean jaEstaNaFila =
                fila.stream().anyMatch(entrada -> entrada.getPaciente().equals(paciente));

        if (jaEstaNaFila) {
            throw new OperacaoInvalidaException(
                    "O paciente já está na lista de espera para este médico nessa data.");
        }

        int posicao = fila.size() + 1;

        ListaDeEspera entrada = new ListaDeEspera(medico, paciente, data, posicao);

        return listaDeEsperaRepository.save(entrada);
    }

    public List<ListaDeEspera> listar(Medico medico, LocalDate data) {

        if (medico == null) {
            throw new IllegalArgumentException("O médico é obrigatório.");
        }

        if (data == null) {
            throw new IllegalArgumentException("A data é obrigatória.");
        }

        return listaDeEsperaRepository.findByMedicoAndDataOrderByPosicaoAsc(medico, data);
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

        ListaDeEspera entrada = primeiroDaFila(medico, data);

        if (entrada == null) {
            return null;
        }

        Paciente paciente = entrada.getPaciente();

        BigDecimal valor = calcularValorConsulta(medico, paciente);

        Consulta consulta = new Consulta(medico, paciente, data, valor);

        Consulta consultaSalva = consultaRepository.save(consulta);

        Notificacao notificacao =
                new Notificacao(
                        paciente,
                        "Você foi promovido da lista de espera. "
                                + "Sua consulta com o médico "
                                + medico.getNome()
                                + " foi agendada para "
                                + data
                                + ".");

        notificacaoRepository.save(notificacao);

        listaDeEsperaRepository.delete(entrada);

        reorganizarFila(medico, data);

        return consultaSalva;
    }

    /**
     * Calcula o valor da consulta respeitando:
     *
     * <p>1. Cobertura do plano aceito pelo médico; 2. Valor particular do médico; 3. Regra de preço
     * da especialidade.
     */
    private BigDecimal calcularValorConsulta(Medico medico, Paciente paciente) {

        Plano plano = paciente.getPlanoSaude();

        // Convênio aceito pelo médico: paciente não paga.
        if (plano != null && medico.aceitaPlano(plano)) {
            return BigDecimal.ZERO;
        }

        // Valor particular definido pelo médico.
        BigDecimal valorBase = medico.getValorConsultaParticular();

        // A especialidade aplica sua própria regra.
        return medico.getEspecialidade().calcularValorConsulta(valorBase);
    }

    private void reorganizarFila(Medico medico, LocalDate data) {

        List<ListaDeEspera> fila =
                listaDeEsperaRepository.findByMedicoAndDataOrderByPosicaoAsc(medico, data);

        for (int i = 0; i < fila.size(); i++) {
            fila.get(i).setPosicao(i + 1);
        }

        listaDeEsperaRepository.saveAll(fila);
    }

    private void validarDados(Medico medico, Paciente paciente, LocalDate data) {

        if (medico == null) {
            throw new IllegalArgumentException("O médico é obrigatório.");
        }

        if (paciente == null) {
            throw new IllegalArgumentException("O paciente é obrigatório.");
        }

        if (paciente.getId() == null) {
            throw new IllegalArgumentException("O paciente informado é inválido.");
        }

        if (data == null) {
            throw new IllegalArgumentException("A data da consulta é obrigatória.");
        }
    }
}
