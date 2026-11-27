package kyj.poo.clinica_medica.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import kyj.poo.clinica_medica.model.EstadoDaConsulta;

public record ConsultaDTO(
    Long id,
    Long medicoId,
    String medicoNome,
    Long pacienteId,
    String pacienteNome,
    LocalDateTime dataHora,
    EstadoDaConsulta estado,
    BigDecimal valorCobrado
) {}
