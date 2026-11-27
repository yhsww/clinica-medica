package kyj.poo.clinica_medica.dto;

import java.math.BigDecimal;

public record MedicoDTO(
    Long id,
    String nome,
    String email,
    String especialidade,
    BigDecimal valorConsultaParticular
) {}
