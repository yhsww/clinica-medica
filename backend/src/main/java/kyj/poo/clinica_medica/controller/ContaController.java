package kyj.poo.clinica_medica.controller;

import java.util.List;
import java.util.stream.Collectors;
import kyj.poo.clinica_medica.dto.ContaDTO;
import kyj.poo.clinica_medica.model.Conta;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.model.Papel;
import kyj.poo.clinica_medica.security.UsuarioAutenticado;
import kyj.poo.clinica_medica.service.ContaService;
import kyj.poo.clinica_medica.service.PacienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contas")
public class ContaController {

    private final ContaService contaService;
    private final PacienteService pacienteService;

    public ContaController(
        ContaService contaService,
        PacienteService pacienteService
    ) {
        this.contaService = contaService;
        this.pacienteService = pacienteService;
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

    /** Contas do paciente logado. situacao = PENDENTE, PAGA ou vazio (todas). */
    @GetMapping("/minhas")
    public ResponseEntity<List<ContaDTO>> minhas(
        @RequestParam(required = false) String situacao,
        Authentication autenticacao
    ) {
        Paciente paciente = pacienteService.buscarPorEmail(
            autenticacao.getName()
        );

        List<Conta> contas;
        if ("PENDENTE".equalsIgnoreCase(situacao)) {
            contas = contaService.listarPendentes(paciente);
        } else if ("PAGA".equalsIgnoreCase(situacao)) {
            contas = contaService.listarPagas(paciente);
        } else {
            contas = contaService.listarPorPaciente(paciente);
        }

        return ResponseEntity.ok(
            contas.stream().map(this::toDto).collect(Collectors.toList())
        );
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

    /** Admin paga qualquer conta; paciente só paga a própria (§9). */
    @PostMapping("/{id}/pagar")
    public ResponseEntity<ContaDTO> pagar(
        @PathVariable Long id,
        Authentication autenticacao
    ) {
        if (UsuarioAutenticado.temPapel(autenticacao, Papel.PACIENTE)) {
            Conta conta = contaService.buscarPorId(id);
            Paciente dono = conta.getConsulta() != null
                ? conta.getConsulta().getPaciente()
                : null;

            if (
                dono == null ||
                !dono.getEmail().equalsIgnoreCase(autenticacao.getName())
            ) {
                throw new AccessDeniedException(
                    "Esta conta não pertence ao paciente."
                );
            }
        }

        return ResponseEntity.ok(toDto(contaService.pagar(id)));
    }

    private ContaDTO toDto(Conta conta) {
        Paciente paciente = conta.getConsulta() != null
            ? conta.getConsulta().getPaciente()
            : null;

        return new ContaDTO(
            conta.getId(),
            conta.getConsulta() != null ? conta.getConsulta().getId() : null,
            paciente != null ? paciente.getId() : null,
            paciente != null ? paciente.getNome() : null,
            conta.getValor(),
            conta.isPaga(),
            conta.isPaga() ? "PAGA" : "PENDENTE"
        );
    }
}
