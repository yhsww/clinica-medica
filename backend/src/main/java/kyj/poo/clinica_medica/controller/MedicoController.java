package kyj.poo.clinica_medica.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import kyj.poo.clinica_medica.dto.AvaliacaoDTO;
import kyj.poo.clinica_medica.dto.MedicoDTO;
import kyj.poo.clinica_medica.dto.MedicoRequest;
import kyj.poo.clinica_medica.exception.OperacaoInvalidaException;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.model.Papel;
import kyj.poo.clinica_medica.model.Plano;
import kyj.poo.clinica_medica.security.UsuarioAutenticado;
import kyj.poo.clinica_medica.service.MedicoService;
import kyj.poo.clinica_medica.service.PacienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medicos")
public class MedicoController {

    private static final int LIMITE_AVALIACOES_RECENTES = 3;

    private final MedicoService medicoService;
    private final PacienteService pacienteService;

    public MedicoController(
        MedicoService medicoService,
        PacienteService pacienteService
    ) {
        this.medicoService = medicoService;
        this.pacienteService = pacienteService;
    }

    /**
     * Pesquisa por nome e/ou especialidade (§4). Paciente com plano de saúde
     * vê apenas os médicos que atendem o plano dele.
     */
    @GetMapping
    public ResponseEntity<List<MedicoDTO>> listar(
        @RequestParam(required = false) String nome,
        @RequestParam(required = false) String especialidade,
        Authentication autenticacao
    ) {
        List<Medico> medicos = medicoService.pesquisar(nome, especialidade);

        if (UsuarioAutenticado.temPapel(autenticacao, Papel.PACIENTE)) {
            Paciente paciente = pacienteService.buscarPorEmail(
                autenticacao.getName()
            );

            if (paciente.possuiPlanoDeSaude()) {
                medicos = medicos
                    .stream()
                    .filter(medico ->
                        medico.aceitaPlano(paciente.getPlanoSaude())
                    )
                    .toList();
            }
        }

        List<MedicoDTO> dtos = medicos.stream().map(this::toDto).toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/me")
    public ResponseEntity<MedicoDTO> meusDados(Authentication autenticacao) {
        return ResponseEntity.ok(
            toDto(medicoService.buscarPorEmail(autenticacao.getName()))
        );
    }

    /** O médico altera apenas os próprios dados; o e-mail (login) não muda. */
    @PutMapping("/me")
    public ResponseEntity<MedicoDTO> atualizarMeusDados(
        @Valid @RequestBody MedicoRequest request,
        Authentication autenticacao
    ) {
        Medico medico = medicoService.buscarPorEmail(autenticacao.getName());

        if (!medico.getEmail().equalsIgnoreCase(request.email().trim())) {
            throw new OperacaoInvalidaException(
                "Não é permitido alterar o e-mail de login."
            );
        }

        return ResponseEntity.ok(
            toDto(medicoService.atualizar(medico.getId(), request))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toDto(medicoService.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<MedicoDTO> criar(
        @Valid @RequestBody MedicoRequest request
    ) {
        Medico medico = medicoService.criar(request);
        return ResponseEntity.created(
            URI.create("/api/medicos/" + medico.getId())
        ).body(toDto(medico));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicoDTO> atualizar(
        @PathVariable Long id,
        @Valid @RequestBody MedicoRequest request
    ) {
        return ResponseEntity.ok(toDto(medicoService.atualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        medicoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private MedicoDTO toDto(Medico medico) {
        return new MedicoDTO(
            medico.getId(),
            medico.getNome(),
            medico.getEmail(),
            medico.getEspecialidade() != null
                ? medico.getEspecialidade().getNome()
                : null,
            medico.getValorConsultaParticular(),
            medico.getPlanosAceitos().stream().map(Plano::getNome).toList(),
            medicoService.calcularMediaAvaliacoes(medico),
            medicoService
                .listarAvaliacoesRecentes(medico, LIMITE_AVALIACOES_RECENTES)
                .stream()
                .map(AvaliacaoDTO::de)
                .toList()
        );
    }
}
