package kyj.poo.clinica_medica.controller;

import java.util.List;
import kyj.poo.clinica_medica.dto.NotificacaoDTO;
import kyj.poo.clinica_medica.model.Notificacao;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.service.NotificacaoService;
import kyj.poo.clinica_medica.service.PacienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final PacienteService pacienteService;

    public NotificacaoController(
        NotificacaoService notificacaoService,
        PacienteService pacienteService
    ) {
        this.notificacaoService = notificacaoService;
        this.pacienteService = pacienteService;
    }

    @GetMapping
    public ResponseEntity<List<NotificacaoDTO>> listar(
        @RequestParam(defaultValue = "false") boolean apenasNaoLidas,
        Authentication autenticacao
    ) {
        Paciente paciente = pacienteService.buscarPorEmail(
            autenticacao.getName()
        );

        List<Notificacao> notificacoes = apenasNaoLidas
            ? notificacaoService.listarNaoLidas(paciente)
            : notificacaoService.listarPorPaciente(paciente);

        return ResponseEntity.ok(
            notificacoes.stream().map(this::toDto).toList()
        );
    }

    @PostMapping("/{id}/lida")
    public ResponseEntity<NotificacaoDTO> marcarComoLida(
        @PathVariable Long id,
        Authentication autenticacao
    ) {
        Notificacao notificacao = notificacaoService.buscarPorId(id);

        if (
            notificacao.getPaciente() == null ||
            !notificacao
                .getPaciente()
                .getEmail()
                .equalsIgnoreCase(autenticacao.getName())
        ) {
            throw new AccessDeniedException(
                "Esta notificação não pertence ao paciente."
            );
        }

        return ResponseEntity.ok(toDto(notificacaoService.marcarComoLida(id)));
    }

    private NotificacaoDTO toDto(Notificacao notificacao) {
        return new NotificacaoDTO(
            notificacao.getId(),
            notificacao.getMensagem(),
            notificacao.getDataHora(),
            notificacao.isLida()
        );
    }
}
