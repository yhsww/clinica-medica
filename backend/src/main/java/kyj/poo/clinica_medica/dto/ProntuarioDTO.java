package kyj.poo.clinica_medica.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProntuarioDTO(
    Long id,
    Long consultaId,
    LocalDateTime data,
    String medicoNome,
    String especialidade,
    String sintomas,
    String diagnostico,
    String tratamento,
    String medicamentos,
    String exames,
    String observacoes,
    BigDecimal valorConsulta
) {}
