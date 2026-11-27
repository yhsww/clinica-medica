package kyj.poo.clinica_medica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kyj.poo.clinica_medica.model.PlanoNome;

public record PacienteRequest(
    @NotBlank(message = "O nome é obrigatório.") String nome,
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "E-mail inválido.")
    String email,
    @NotNull(message = "A idade é obrigatória.")
    @Positive(message = "A idade deve ser maior que zero.")
    Integer idade,
    PlanoNome planoSaude
) {}
