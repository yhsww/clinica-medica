package kyj.poo.clinica_medica.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import kyj.poo.clinica_medica.dto.EstatisticasDTO;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.especialidade.Especialidade;
import kyj.poo.clinica_medica.service.EstatisticaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estatisticas")
public class EstatisticaController {

    private final EstatisticaService estatisticaService;

    public EstatisticaController(EstatisticaService estatisticaService) {
        this.estatisticaService = estatisticaService;
    }

    @GetMapping
    public ResponseEntity<EstatisticasDTO> obter() {
        Optional<Medico> melhor = estatisticaService.medicoMaisBemAvaliado();

        List<EstatisticasDTO.ConsultasPorMedico> porMedico = estatisticaService
            .consultasPorMedico()
            .entrySet()
            .stream()
            .map(entrada ->
                new EstatisticasDTO.ConsultasPorMedico(
                    entrada.getKey().getId(),
                    entrada.getKey().getNome(),
                    entrada.getValue()
                )
            )
            .sorted(
                Comparator.comparingLong(
                    EstatisticasDTO.ConsultasPorMedico::quantidade
                ).reversed()
            )
            .toList();

        EstatisticasDTO dto = new EstatisticasDTO(
            melhor.map(Medico::getNome).orElse(null),
            melhor.map(estatisticaService::calcularMediaDoMedico).orElse(null),
            estatisticaService
                .especialidadeMaisProcurada()
                .map(Especialidade::getNome)
                .orElse(null),
            estatisticaService.totalConsultasRealizadas(),
            estatisticaService.mediaGeralAvaliacoes(),
            porMedico
        );

        return ResponseEntity.ok(dto);
    }
}
