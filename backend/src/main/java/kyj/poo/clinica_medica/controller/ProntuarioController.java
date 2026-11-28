package kyj.poo.clinica_medica.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import kyj.poo.clinica_medica.dto.ProntuarioDTO;
import kyj.poo.clinica_medica.dto.ProntuarioRequest;
import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.model.Papel;
import kyj.poo.clinica_medica.model.Prontuario;
import kyj.poo.clinica_medica.security.UsuarioAutenticado;
import kyj.poo.clinica_medica.service.ConsultaService;
import kyj.poo.clinica_medica.service.MedicoService;
import kyj.poo.clinica_medica.service.PacienteService;
import kyj.poo.clinica_medica.service.ProntuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prontuarios")
public class ProntuarioController {

    private final ProntuarioService prontuarioService;
    private final ConsultaService consultaService;
    private final PacienteService pacienteService;
    private final MedicoService medicoService;

    public ProntuarioController(
        ProntuarioService prontuarioService,
        ConsultaService consultaService,
        PacienteService pacienteService,
        MedicoService medicoService
    ) {
        this.prontuarioService = prontuarioService;
        this.consultaService = consultaService;
        this.pacienteService = pacienteService;
        this.medicoService = medicoService;
    }

    /** O médico registra o atendimento depois de marcar a consulta como REALIZADA. */
    @PostMapping
    public ResponseEntity<ProntuarioDTO> registrar(
        @Valid @RequestBody ProntuarioRequest request,
        Authentication autenticacao
    ) {
        Consulta consulta = consultaService.buscarPorId(request.consultaId());

        if (UsuarioAutenticado.temPapel(autenticacao, Papel.MEDICO)) {
            Medico medico = medicoService.buscarPorEmail(
                autenticacao.getName()
            );
            if (!consultaService.pertenceAoMedico(consulta, medico)) {
                throw new AccessDeniedException(
                    "Somente o médico responsável pode registrar este prontuário."
                );
            }
        }

        Prontuario prontuario = prontuarioService.criar(
            consulta,
            request.sintomas(),
            request.diagnostico(),
            request.tratamento(),
            request.medicamentos(),
            request.exames(),
            request.observacoes()
        );

        return ResponseEntity.created(
            URI.create("/api/prontuarios/consulta/" + consulta.getId())
        ).body(toDto(prontuario));
    }

    @GetMapping("/consulta/{consultaId}")
    public ResponseEntity<ProntuarioDTO> buscarPorConsulta(
        @PathVariable Long consultaId,
        Authentication autenticacao
    ) {
        Consulta consulta = consultaService.buscarPorId(consultaId);
        verificarAcesso(consulta.getPaciente(), autenticacao);

        return ResponseEntity.ok(
            toDto(prontuarioService.buscarPorConsulta(consulta))
        );
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<ProntuarioDTO>> historico(
        @PathVariable Long pacienteId,
        Authentication autenticacao
    ) {
        Paciente paciente = pacienteService.buscarPorId(pacienteId);
        verificarAcesso(paciente, autenticacao);

        return ResponseEntity.ok(listar(paciente));
    }

    @GetMapping("/meu")
    public ResponseEntity<List<ProntuarioDTO>> meuHistorico(
        Authentication autenticacao
    ) {
        Paciente paciente = pacienteService.buscarPorEmail(
            autenticacao.getName()
        );

        return ResponseEntity.ok(listar(paciente));
    }

    private List<ProntuarioDTO> listar(Paciente paciente) {
        return prontuarioService
            .listarHistorico(paciente)
            .stream()
            .map(this::toDto)
            .toList();
    }

    /** §7: acesso só do próprio paciente, dos médicos que o atendem e do admin. */
    private void verificarAcesso(Paciente paciente, Authentication autenticacao) {
        if (UsuarioAutenticado.temPapel(autenticacao, Papel.ADMIN)) {
            return;
        }

        if (UsuarioAutenticado.temPapel(autenticacao, Papel.PACIENTE)) {
            if (
                paciente == null ||
                !paciente.getEmail().equalsIgnoreCase(autenticacao.getName())
            ) {
                throw new AccessDeniedException(
                    "Você só pode acessar o seu próprio prontuário."
                );
            }
            return;
        }

        if (UsuarioAutenticado.temPapel(autenticacao, Papel.MEDICO)) {
            Medico medico = medicoService.buscarPorEmail(
                autenticacao.getName()
            );
            if (!consultaService.medicoAtendePaciente(medico, paciente)) {
                throw new AccessDeniedException(
                    "O médico não atende este paciente."
                );
            }
            return;
        }

        throw new AccessDeniedException("Acesso negado ao prontuário.");
    }

    private ProntuarioDTO toDto(Prontuario prontuario) {
        Consulta consulta = prontuario.getConsulta();
        Medico medico = consulta != null ? consulta.getMedico() : null;

        return new ProntuarioDTO(
            prontuario.getId(),
            consulta != null ? consulta.getId() : null,
            consulta != null ? consulta.getDataHora() : null,
            medico != null ? medico.getNome() : null,
            medico != null && medico.getEspecialidade() != null
                ? medico.getEspecialidade().getNome()
                : null,
            prontuario.getSintomas(),
            prontuario.getDiagnostico(),
            prontuario.getTratamento(),
            prontuario.getMedicamentos(),
            prontuario.getExames(),
            prontuario.getObservacoes(),
            consulta != null ? consulta.getValorCobrado() : null
        );
    }
}
