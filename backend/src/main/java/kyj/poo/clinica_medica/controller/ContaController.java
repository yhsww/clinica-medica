package kyj.poo.clinica_medica.controller;

import java.util.List;
import java.util.stream.Collectors;
import kyj.poo.clinica_medica.dto.ContaDTO;
import kyj.poo.clinica_medica.model.Conta;
import kyj.poo.clinica_medica.service.ContaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contas")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @GetMapping
    public ResponseEntity<List<ContaDTO>> listar() {
        List<ContaDTO> dtos = contaService
            .listarTodos()
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toDto(contaService.buscarPorId(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        contaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<ContaDTO> pagar(@PathVariable Long id) {
        return ResponseEntity.ok(toDto(contaService.pagar(id)));
    }

    private ContaDTO toDto(Conta conta) {
        return new ContaDTO(
            conta.getId(),
            conta.getConsulta() != null ? conta.getConsulta().getId() : null,
            conta.getValor(),
            conta.isPaga()
        );
    }
}
