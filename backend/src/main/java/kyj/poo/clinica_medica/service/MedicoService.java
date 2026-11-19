package kyj.poo.clinica_medica.service;

import kyj.poo.clinica_medica.exception.RecursoNaoEncontradoException;
import kyj.poo.clinica_medica.model.Avaliacao;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Plano;
import kyj.poo.clinica_medica.model.especialidade.Especialidade;
import kyj.poo.clinica_medica.repository.AvaliacaoRepository;
import kyj.poo.clinica_medica.repository.MedicoRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    public MedicoService(
            MedicoRepository medicoRepository, AvaliacaoRepository avaliacaoRepository) {

        this.medicoRepository = medicoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    public Medico salvar(Medico medico) {

        if (medico == null) {
            throw new IllegalArgumentException("O médico é obrigatório.");
        }

        if (medico.getNome() == null || medico.getNome().isBlank()) {

            throw new IllegalArgumentException("O nome do médico é obrigatório.");
        }

        if (medico.getEspecialidade() == null) {
            throw new IllegalArgumentException("A especialidade do médico é obrigatória.");
        }

        if (medico.getValorConsultaParticular() == null
                || medico.getValorConsultaParticular().compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException("O valor da consulta particular deve ser válido.");
        }

        return medicoRepository.save(medico);
    }

    public Medico buscarPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("O ID do médico é obrigatório.");
        }

        return medicoRepository
                .findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Médico não encontrado."));
    }

    public List<Medico> buscarPorNome(String nome) {

        if (nome == null || nome.isBlank()) {
            return medicoRepository.findAll();
        }

        return medicoRepository.findByNomeContainingIgnoreCase(nome);
    }

    public List<Medico> buscarPorEspecialidade(Especialidade especialidade) {

        if (especialidade == null) {
            throw new IllegalArgumentException("A especialidade é obrigatória.");
        }

        return medicoRepository.findByEspecialidade(especialidade);
    }

    public List<Medico> buscar(String nome, Especialidade especialidade) {

        boolean possuiNome = nome != null && !nome.isBlank();

        boolean possuiEspecialidade = especialidade != null;

        if (possuiNome && possuiEspecialidade) {
            return medicoRepository.findByNomeContainingIgnoreCaseAndEspecialidade(
                    nome, especialidade);
        }

        if (possuiNome) {
            return buscarPorNome(nome);
        }

        if (possuiEspecialidade) {
            return buscarPorEspecialidade(especialidade);
        }

        return medicoRepository.findAll();
    }

    public List<Medico> buscarPorPlano(Plano plano) {

        if (plano == null) {
            throw new IllegalArgumentException("O plano é obrigatório.");
        }

        return medicoRepository.findByPlanosAceitosContaining(plano);
    }

    public double calcularMediaAvaliacoes(Medico medico) {

        if (medico == null) {
            throw new IllegalArgumentException("O médico é obrigatório.");
        }

        List<Avaliacao> avaliacoes =
                avaliacaoRepository.findAll().stream()
                        .filter(
                                avaliacao ->
                                        avaliacao.getMedico() != null
                                                && avaliacao.getMedico().equals(medico))
                        .toList();

        if (avaliacoes.isEmpty()) {
            return 0.0;
        }

        return avaliacoes.stream().mapToInt(Avaliacao::getEstrelas).average().orElse(0.0);
    }
}
