package kyj.poo.clinica_medica.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import kyj.poo.clinica_medica.dto.MedicoDTO;
import kyj.poo.clinica_medica.dto.MedicoRequest;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.service.MedicoService;
import org.springframework.http.ResponseEntity;
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

    private final MedicoService medicoService;

    public MedicoController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }

    @GetMapping
    public ResponseEntity<List<MedicoDTO>> listar(
        @RequestParam(required = false) String nome
    ) {
        List<Medico> medicos = medicoService.buscar(nome, null);

        List<MedicoDTO> dtos = medicos
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
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
            medico.getValorConsultaParticular()
        );
    }
}
