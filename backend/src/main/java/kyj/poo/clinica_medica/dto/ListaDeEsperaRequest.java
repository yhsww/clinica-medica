
package kyj.poo.clinica_medica.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record ListaDeEsperaRequest(
        @NotBlank(message = "O médico é obrigatório.")
        Long medicoId,

        @NotBlank(message = "O paciente é obrigatório.")
        Long pacienteId,

        @NotBlank(message = "A data e o horário são obrigatórios.")
        @Future(message = "A data e o horário devem ser futuros.")
        LocalDateTime dataHora
) {}
