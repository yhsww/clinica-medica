package kyj.poo.clinica_medica.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import kyj.poo.clinica_medica.dto.AvaliacaoDTO;
import kyj.poo.clinica_medica.dto.AvaliacaoRequest;
import kyj.poo.clinica_medica.model.Avaliacao;
import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.service.AvaliacaoService;
import kyj.poo.clinica_medica.service.ConsultaService;
import kyj.poo.clinica_medica.service.MedicoService;
import kyj.poo.clinica_medica.service.PacienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;
    private final ConsultaService consultaService;
    private final PacienteService pacienteService;
    private final MedicoService medicoService;

    public AvaliacaoController(
        AvaliacaoService avaliacaoService,
        ConsultaService consultaService,
        PacienteService pacienteService,
        MedicoService medicoService
    ) {
        this.avaliacaoService = avaliacaoService;
        this.consultaService = consultaService;
        this.pacienteService = pacienteService;
        this.medicoService = medicoService;
    }

    /** O paciente vem do token: ninguém avalia em nome de outro. */
    @PostMapping
    public ResponseEntity<AvaliacaoDTO> avaliar(
        @Valid @RequestBody AvaliacaoRequest request,
        Authentication autenticacao
    ) {
        Paciente paciente = pacienteService.buscarPorEmail(
            autenticacao.getName()
        );
        Consulta consulta = consultaService.buscarPorId(request.consultaId());

        Avaliacao avaliacao = avaliacaoService.avaliar(
            consulta,
            paciente,
            request.estrelas(),
            request.comentario()
        );

        return ResponseEntity.created(
            URI.create("/api/avaliacoes/" + avaliacao.getId())
        ).body(AvaliacaoDTO.de(avaliacao));
    }

    @GetMapping("/medico/{medicoId}")
    public ResponseEntity<List<AvaliacaoDTO>> listarPorMedico(
        @PathVariable Long medicoId
    ) {
        List<AvaliacaoDTO> dtos = avaliacaoService
            .listarPorMedico(medicoService.buscarPorId(medicoId))
            .stream()
            .map(AvaliacaoDTO::de)
            .toList();

        return ResponseEntity.ok(dtos);
    }
}
