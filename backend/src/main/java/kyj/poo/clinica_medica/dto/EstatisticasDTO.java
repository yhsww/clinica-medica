package kyj.poo.clinica_medica.dto;

import java.util.List;

public record EstatisticasDTO(
    String medicoMaisBemAvaliado,
    Double mediaMedicoMaisBemAvaliado,
    String especialidadeMaisProcurada,
    long totalConsultasRealizadas,
    double mediaGeralAvaliacoes,
    List<ConsultasPorMedico> consultasRealizadasPorMedico
) {
    public record ConsultasPorMedico(
        Long medicoId,
        String medicoNome,
        long quantidade
    ) {}
}
