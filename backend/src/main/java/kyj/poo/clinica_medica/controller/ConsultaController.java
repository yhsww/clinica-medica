package kyj.poo.clinica_medica.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import kyj.poo.clinica_medica.dto.ConsultaDTO;
import kyj.poo.clinica_medica.dto.ConsultaRequest;
import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.service.ConsultaService;
import kyj.poo.clinica_medica.service.MedicoService;
import kyj.poo.clinica_medica.service.PacienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;
    private final PacienteService pacienteService;
    private final MedicoService medicoService;

    public ConsultaController(
        ConsultaService consultaService,
        PacienteService pacienteService,
        MedicoService medicoService
    ) {
        this.consultaService = consultaService;
        this.pacienteService = pacienteService;
        this.medicoService = medicoService;
    }

    @GetMapping
    public ResponseEntity<List<ConsultaDTO>> listar(
        Authentication autenticacao
    ) {
        return minhas(autenticacao);
    }

    /** Consultas do usuário logado (paciente ou médico); admin vê todas. */
    @GetMapping("/minhas")
    public ResponseEntity<List<ConsultaDTO>> minhas(
        Authentication autenticacao
    ) {
        List<Consulta> consultas;

        if (temPapel(autenticacao, "ADMIN")) {
            consultas = consultaService.listarTodos();
        } else if (temPapel(autenticacao, "MEDICO")) {
            consultas = consultaService.listarPorMedico(
                medicoService.buscarPorEmail(autenticacao.getName())
            );
        } else {
            consultas = consultaService.listarPorPaciente(
                pacienteService.buscarPorEmail(autenticacao.getName())
            );
        }

        return ResponseEntity.ok(
            consultas.stream().map(this::toDto).collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultaDTO> buscarPorId(
        @PathVariable Long id,
        Authentication autenticacao
    ) {
        verificarPropriedade(id, autenticacao);
        return ResponseEntity.ok(toDto(consultaService.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<ConsultaDTO> criar(
        @Valid @RequestBody ConsultaRequest request,
        Authentication autenticacao
    ) {
        if (temPapel(autenticacao, "PACIENTE")) {
            Long pacienteId = pacienteService
                .buscarPorEmail(autenticacao.getName())
                .getId();
            if (!pacienteId.equals(request.pacienteId())) {
                throw new AccessDeniedException(
                    "O paciente pode agendar apenas suas próprias consultas."
                );
            }
        }

        Consulta consulta = consultaService.criar(request);
        return ResponseEntity.created(
            URI.create("/api/consultas/" + consulta.getId())
        ).body(toDto(consulta));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsultaDTO> atualizar(
        @PathVariable Long id,
        @Valid @RequestBody ConsultaRequest request
    ) {
        return ResponseEntity.ok(toDto(consultaService.atualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        consultaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/realizar")
    public ResponseEntity<ConsultaDTO> realizar(
        @PathVariable Long id,
        Authentication autenticacao
    ) {
        verificarPropriedade(id, autenticacao);
        return ResponseEntity.ok(toDto(consultaService.realizar(id)));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<ConsultaDTO> cancelar(
        @PathVariable Long id,
        Authentication autenticacao
    ) {
        verificarPropriedade(id, autenticacao);
        return ResponseEntity.ok(toDto(consultaService.cancelar(id)));
    }

    private void verificarPropriedade(Long id, Authentication autenticacao) {
        if (autenticacao == null || temPapel(autenticacao, "ADMIN")) {
            return;
        }

        Consulta consulta = consultaService.buscarPorId(id);
        String email = autenticacao.getName();

        if (temPapel(autenticacao, "PACIENTE")) {
            if (
                consulta.getPaciente() == null ||
                !email.equalsIgnoreCase(consulta.getPaciente().getEmail())
            ) {
                throw new AccessDeniedException(
                    "Esta consulta não pertence ao paciente."
                );
            }
        } else if (temPapel(autenticacao, "MEDICO")) {
            if (
                consulta.getMedico() == null ||
                !email.equalsIgnoreCase(consulta.getMedico().getEmail())
            ) {
                throw new AccessDeniedException(
                    "Esta consulta não pertence ao médico."
                );
            }
        }
    }

    private boolean temPapel(Authentication autenticacao, String papel) {
        if (autenticacao == null) {
            return false;
        }

        return autenticacao
            .getAuthorities()
            .stream()
            .anyMatch(autoridade ->
                autoridade.getAuthority().equals("ROLE_" + papel)
            );
    }

    private ConsultaDTO toDto(Consulta consulta) {
        return new ConsultaDTO(
            consulta.getId(),
            consulta.getMedico() != null ? consulta.getMedico().getId() : null,
            consulta.getMedico() != null
                ? consulta.getMedico().getNome()
                : null,
            consulta.getPaciente() != null
                ? consulta.getPaciente().getId()
                : null,
            consulta.getPaciente() != null
                ? consulta.getPaciente().getNome()
                : null,
            consulta.getDataHora(),
            consulta.getEstado(),
            consulta.getValorCobrado()
        );
    }
}
