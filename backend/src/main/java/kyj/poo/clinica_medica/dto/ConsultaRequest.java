package kyj.poo.clinica_medica.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ConsultaRequest(
    @NotNull(message = "O ID do médico é obrigatório.") Long medicoId,
    @NotNull(message = "O ID do paciente é obrigatório.") Long pacienteId,
    @NotNull(message = "A data da consulta é obrigatória.")
    LocalDateTime dataHora
) {}
