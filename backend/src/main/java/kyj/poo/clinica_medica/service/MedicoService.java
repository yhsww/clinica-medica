package kyj.poo.clinica_medica.service;

import java.math.BigDecimal;
import java.util.List;
import kyj.poo.clinica_medica.dto.MedicoRequest;
import kyj.poo.clinica_medica.exception.OperacaoInvalidaException;
import kyj.poo.clinica_medica.exception.RecursoNaoEncontradoException;
import kyj.poo.clinica_medica.model.Avaliacao;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Plano;
import kyj.poo.clinica_medica.model.PlanoNome;
import kyj.poo.clinica_medica.model.especialidade.Especialidade;
import kyj.poo.clinica_medica.repository.AvaliacaoRepository;
import kyj.poo.clinica_medica.repository.ConsultaRepository;
import kyj.poo.clinica_medica.repository.CredencialRepository;
import kyj.poo.clinica_medica.repository.EspecialidadeRepository;
import kyj.poo.clinica_medica.repository.MedicoRepository;
import kyj.poo.clinica_medica.repository.PlanoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final CredencialRepository credencialRepository;
    private final ConsultaRepository consultaRepository;
    private final PlanoRepository planoRepository;

    public MedicoService(
        MedicoRepository medicoRepository,
        AvaliacaoRepository avaliacaoRepository,
        EspecialidadeRepository especialidadeRepository,
        CredencialRepository credencialRepository,
        ConsultaRepository consultaRepository,
        PlanoRepository planoRepository
    ) {
        this.medicoRepository = medicoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.especialidadeRepository = especialidadeRepository;
        this.credencialRepository = credencialRepository;
        this.consultaRepository = consultaRepository;
        this.planoRepository = planoRepository;
    }

    @Transactional
    public Medico criar(MedicoRequest request) {
        Medico medico = new Medico();
        aplicar(medico, request);
        return salvar(medico);
    }

    @Transactional
    public Medico atualizar(Long id, MedicoRequest request) {
        Medico medico = buscarPorId(id);
        aplicar(medico, request);
        return salvar(medico);
    }

    @Transactional
    public void excluir(Long id) {
        Medico medico = buscarPorId(id);

        if (consultaRepository.existsByMedico_Id(id)) {
            throw new OperacaoInvalidaException(
                "Não é possível excluir o médico: existem consultas associadas."
            );
        }

        credencialRepository
            .findByUsuario_Id(id)
            .ifPresent(credencialRepository::delete);
        medicoRepository.delete(medico);
    }

    private void aplicar(Medico medico, MedicoRequest request) {
        medico.setNome(request.nome().trim());
        medico.setEmail(request.email().trim());
        medico.setEspecialidade(
            resolverEspecialidade(request.especialidade().getNome())
        );
        medico.setValorConsultaParticular(request.valorConsultaParticular());

        // null = manter os planos atuais
        if (request.planosAceitos() != null) {
            medico.getPlanosAceitos().clear();
            for (PlanoNome planoNome : request.planosAceitos()) {
                medico.adicionarPlanoAceito(resolverPlano(planoNome.getNome()));
            }
        }
    }

    private Especialidade resolverEspecialidade(String nome) {
        return especialidadeRepository
            .findByNomeIgnoreCase(nome.trim())
            .orElseThrow(() ->
                new OperacaoInvalidaException("Especialidade inválida: " + nome)
            );
    }

    private Plano resolverPlano(String nome) {
        return planoRepository
            .findByNomeIgnoreCase(nome.trim())
            .orElseThrow(() ->
                new OperacaoInvalidaException("Plano inválido: " + nome)
            );
    }

    public Medico salvar(Medico medico) {
        if (medico == null) {
            throw new IllegalArgumentException("O médico é obrigatório.");
        }

        if (medico.getNome() == null || medico.getNome().isBlank()) {
            throw new IllegalArgumentException(
                "O nome do médico é obrigatório."
            );
        }

        if (medico.getEspecialidade() == null) {
            throw new IllegalArgumentException(
                "A especialidade do médico é obrigatória."
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

        return medicoRepository.save(medico);
    }

    public Medico buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("O ID do médico é obrigatório.");
        }

        return medicoRepository
            .findById(id)
            .orElseThrow(() ->
                new RecursoNaoEncontradoException("Médico não encontrado.")
            );
    }

    public Medico buscarPorEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("O e-mail é obrigatório.");
        }

        return medicoRepository
            .findByEmail(email)
            .stream()
            .findFirst()
            .orElseThrow(() ->
                new RecursoNaoEncontradoException("Médico não encontrado.")
            );
    }

    public List<Medico> buscarPorNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return medicoRepository.findAll();
        }

        return medicoRepository.findByNomeContainingIgnoreCase(nome);
    }

    public List<Medico> buscarPorEspecialidade(Especialidade especialidade) {
        if (especialidade == null) {
            throw new IllegalArgumentException(
                "A especialidade é obrigatória."
            );
        }

        return medicoRepository.findByEspecialidade(especialidade);
    }

    public List<Medico> buscar(String nome, Especialidade especialidade) {
        boolean possuiNome = nome != null && !nome.isBlank();

        boolean possuiEspecialidade = especialidade != null;

        if (possuiNome && possuiEspecialidade) {
            return medicoRepository.findByNomeContainingIgnoreCaseAndEspecialidade(
                nome,
                especialidade
            );
        }

        if (possuiNome) {
            return buscarPorNome(nome);
        }

        if (possuiEspecialidade) {
            return buscarPorEspecialidade(especialidade);
        }

        return medicoRepository.findAll();
    }

    /**
     * Pesquisa por nome e/ou nome da especialidade (ex.: "Pediatria").
     * Tem nome diferente de buscar(...) para não gerar ambiguidade em
     * chamadas como buscar(nome, null).
     */
    public List<Medico> pesquisar(String nome, String especialidadeNome) {
        Especialidade especialidade =
            especialidadeNome == null || especialidadeNome.isBlank()
                ? null
                : resolverEspecialidade(especialidadeNome);

        return buscar(nome, especialidade);
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

        List<Avaliacao> avaliacoes = avaliacaoRepository
            .findAll()
            .stream()
            .filter(
                avaliacao ->
                    avaliacao.getMedico() != null &&
                    avaliacao.getMedico().equals(medico)
            )
            .toList();

        if (avaliacoes.isEmpty()) {
            return 0.0;
        }

        return avaliacoes
            .stream()
            .mapToInt(Avaliacao::getEstrelas)
            .average()
            .orElse(0.0);
    }

    public List<Avaliacao> listarAvaliacoesRecentes(Medico medico, int limite) {
        if (medico == null) {
            throw new IllegalArgumentException("O médico é obrigatório.");
        }

        return avaliacaoRepository
            .findByMedicoOrderByDataHoraDescIdDesc(medico)
            .stream()
            .limit(limite)
            .toList();
    }
}
