package kyj.poo.clinica_medica.service;

import kyj.poo.clinica_medica.model.Avaliacao;
import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.EstadoDaConsulta;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.especialidade.Especialidade;
import kyj.poo.clinica_medica.repository.AvaliacaoRepository;
import kyj.poo.clinica_medica.repository.ConsultaRepository;
import kyj.poo.clinica_medica.repository.MedicoRepository;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EstatisticaService {

    private final MedicoRepository medicoRepository;
    private final ConsultaRepository consultaRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    public EstatisticaService(
            MedicoRepository medicoRepository,
            ConsultaRepository consultaRepository,
            AvaliacaoRepository avaliacaoRepository) {

        this.medicoRepository = medicoRepository;
        this.consultaRepository = consultaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    public Optional<Medico> medicoMaisBemAvaliado() {

        List<Medico> medicos = medicoRepository.findAll();

        return medicos.stream()
                .filter(medico -> !avaliacoesDoMedico(medico).isEmpty())
                .max(Comparator.comparingDouble(this::calcularMediaDoMedico));
    }

    public double calcularMediaDoMedico(Medico medico) {

        if (medico == null) {
            throw new IllegalArgumentException("O médico é obrigatório.");
        }

        List<Avaliacao> avaliacoes = avaliacoesDoMedico(medico);

        if (avaliacoes.isEmpty()) {
            return 0.0;
        }

        return avaliacoes.stream().mapToInt(Avaliacao::getEstrelas).average().orElse(0.0);
    }

    public double mediaGeralAvaliacoes() {

        List<Avaliacao> avaliacoes = avaliacaoRepository.findAll();

        if (avaliacoes.isEmpty()) {
            return 0.0;
        }

        return avaliacoes.stream().mapToInt(Avaliacao::getEstrelas).average().orElse(0.0);
    }

    public long totalConsultasRealizadas() {

        return consultaRepository.findAll().stream()
                .filter(consulta -> consulta.getEstado() == EstadoDaConsulta.REALIZADA)
                .count();
    }

    public Map<Medico, Long> consultasPorMedico() {

        return consultaRepository.findAll().stream()
                .filter(consulta -> consulta.getEstado() == EstadoDaConsulta.REALIZADA)
                .collect(Collectors.groupingBy(Consulta::getMedico, Collectors.counting()));
    }

    public Optional<Especialidade> especialidadeMaisProcurada() {

        Map<Especialidade, Long> quantidadePorEspecialidade =
                consultaRepository.findAll().stream()
                        .filter(consulta -> consulta.getEstado() == EstadoDaConsulta.REALIZADA)
                        .filter(
                                consulta ->
                                        consulta.getMedico() != null
                                                && consulta.getMedico().getEspecialidade() != null)
                        .collect(
                                Collectors.groupingBy(
                                        consulta -> consulta.getMedico().getEspecialidade(),
                                        Collectors.counting()));

        return quantidadePorEspecialidade.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    private List<Avaliacao> avaliacoesDoMedico(Medico medico) {

        return avaliacaoRepository.findAll().stream()
                .filter(
                        avaliacao ->
                                avaliacao.getMedico() != null
                                        && avaliacao.getMedico().equals(medico))
                .toList();
    }
}
