package kyj.poo.clinica_medica.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import kyj.poo.clinica_medica.dto.PacienteDTO;
import kyj.poo.clinica_medica.dto.PacienteRequest;
import kyj.poo.clinica_medica.exception.OperacaoInvalidaException;
import kyj.poo.clinica_medica.model.Paciente;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping
    public ResponseEntity<List<PacienteDTO>> listar() {
        List<Paciente> pacientes = pacienteService
            .listarTodos()
            .stream()
            .filter(p -> !p.getEmail().equals("admin@clinica.com"))
            .collect(Collectors.toList());

        List<PacienteDTO> dtos = pacientes
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/me")
    public ResponseEntity<PacienteDTO> meusDados(Authentication autenticacao) {
        return ResponseEntity.ok(
            toDto(pacienteService.buscarPorEmail(autenticacao.getName()))
        );
    }

    /** O paciente altera apenas os próprios dados; o e-mail (login) não muda. */
    @PutMapping("/me")
    public ResponseEntity<PacienteDTO> atualizarMeusDados(
        @Valid @RequestBody PacienteRequest request,
        Authentication autenticacao
    ) {
        Paciente paciente = pacienteService.buscarPorEmail(
            autenticacao.getName()
        );

        if (!paciente.getEmail().equalsIgnoreCase(request.email().trim())) {
            throw new OperacaoInvalidaException(
                "Não é permitido alterar o e-mail de login."
            );
        }

        return ResponseEntity.ok(
            toDto(pacienteService.atualizar(paciente.getId(), request))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toDto(pacienteService.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<PacienteDTO> criar(
        @Valid @RequestBody PacienteRequest request
    ) {
        Paciente paciente = pacienteService.criar(request);
        return ResponseEntity.created(
            URI.create("/api/pacientes/" + paciente.getId())
        ).body(toDto(paciente));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteDTO> atualizar(
        @PathVariable Long id,
        @Valid @RequestBody PacienteRequest request
    ) {
        return ResponseEntity.ok(toDto(pacienteService.atualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        pacienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private PacienteDTO toDto(Paciente paciente) {
        return new PacienteDTO(
            paciente.getId(),
            paciente.getNome(),
            paciente.getEmail(),
            paciente.getIdade(),
            paciente.getPlanoSaude() != null
                ? paciente.getPlanoSaude().getNome()
                : null
        );
    }
}
