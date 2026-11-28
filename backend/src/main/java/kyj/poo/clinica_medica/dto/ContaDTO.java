package kyj.poo.clinica_medica.dto;

import java.math.BigDecimal;

public record ContaDTO(
    Long id,
    Long consultaId,
    Long pacienteId,
    String pacienteNome,
    BigDecimal valor,
    boolean paga,
    String situacao
) {}
