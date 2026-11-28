package kyj.poo.clinica_medica.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @NotNull em vez de @NotBlank: @NotBlank só vale para texto e lança
 * erro em Long/LocalDateTime quando o objeto é validado.
 */
public record ListaDeEsperaRequest(
        @NotNull(message = "O médico é obrigatório.")
        Long medicoId,

        @NotNull(message = "O paciente é obrigatório.")
        Long pacienteId,

        @NotNull(message = "A data e o horário são obrigatórios.")
        @Future(message = "A data e o horário devem ser futuros.")
        LocalDateTime dataHora
) {}
