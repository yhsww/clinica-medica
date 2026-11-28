package kyj.poo.clinica_medica.controller;

import java.time.LocalDate;
import java.util.List;
import kyj.poo.clinica_medica.dto.ListaDeEsperaDTO;
import kyj.poo.clinica_medica.model.ListaDeEspera;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Papel;
import kyj.poo.clinica_medica.security.UsuarioAutenticado;
import kyj.poo.clinica_medica.service.ListaDeEsperaService;
import kyj.poo.clinica_medica.service.MedicoService;
import kyj.poo.clinica_medica.service.PacienteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lista-espera")
public class ListaDeEsperaController {

    private final ListaDeEsperaService listaDeEsperaService;
    private final PacienteService pacienteService;
    private final MedicoService medicoService;

    public ListaDeEsperaController(
        ListaDeEsperaService listaDeEsperaService,
        PacienteService pacienteService,
        MedicoService medicoService
    ) {
        this.listaDeEsperaService = listaDeEsperaService;
        this.pacienteService = pacienteService;
        this.medicoService = medicoService;
    }

    /** Filas em que o paciente logado está, com a posição atual. */
    @GetMapping("/minha")
    public ResponseEntity<List<ListaDeEsperaDTO>> minha(
        Authentication autenticacao
    ) {
        List<ListaDeEsperaDTO> dtos = listaDeEsperaService
            .listarPorPaciente(
                pacienteService.buscarPorEmail(autenticacao.getName())
            )
            .stream()
            .map(this::toDto)
            .toList();

        return ResponseEntity.ok(dtos);
    }

    /** Fila de um médico num dia (ex.: ?medicoId=1&data=2026-12-07). */
    @GetMapping
    public ResponseEntity<List<ListaDeEsperaDTO>> porMedicoEData(
        @RequestParam Long medicoId,
        @RequestParam @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
        ) LocalDate data,
        Authentication autenticacao
    ) {
        Medico medico = medicoService.buscarPorId(medicoId);

        boolean admin = UsuarioAutenticado.temPapel(autenticacao, Papel.ADMIN);
        boolean medicoDono =
            UsuarioAutenticado.temPapel(autenticacao, Papel.MEDICO) &&
            medico.getEmail().equalsIgnoreCase(autenticacao.getName());

        if (!admin && !medicoDono) {
            throw new AccessDeniedException(
                "Acesso restrito ao médico responsável ou ao administrador."
            );
        }

        List<ListaDeEsperaDTO> dtos = listaDeEsperaService
            .listar(medico, data)
            .stream()
            .map(this::toDto)
            .toList();

        return ResponseEntity.ok(dtos);
    }

    private ListaDeEsperaDTO toDto(ListaDeEspera entrada) {
        return new ListaDeEsperaDTO(
            entrada.getId(),
            entrada.getMedico() != null ? entrada.getMedico().getId() : null,
            entrada.getMedico() != null ? entrada.getMedico().getNome() : null,
            entrada.getPaciente() != null
                ? entrada.getPaciente().getId()
                : null,
            entrada.getPaciente() != null
                ? entrada.getPaciente().getNome()
                : null,
            entrada.getDataHora(),
            entrada.getPosicao()
        );
    }
}
