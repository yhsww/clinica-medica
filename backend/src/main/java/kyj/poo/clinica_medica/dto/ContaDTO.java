package kyj.poo.clinica_medica.dto;

import java.math.BigDecimal;

public record ContaDTO(
    Long id,
    Long consultaId,
    BigDecimal valor,
    boolean paga
) {}
