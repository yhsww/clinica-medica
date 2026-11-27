package kyj.poo.clinica_medica.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import kyj.poo.clinica_medica.model.PlanoNome;
import kyj.poo.clinica_medica.model.especialidade.EspecialidadeNome;

public record CadastroRequest(
    @NotBlank(message = "O nome é obrigatório.") String nome,
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "E-mail inválido.")
    String email,
    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 4, message = "A senha deve ter ao menos 4 caracteres.")
    String senha,
    String tipo,
    Integer idade,
    PlanoNome planoSaude,
    EspecialidadeNome especialidade,
    @DecimalMin(
        value = "0.00",
        message = "O valor deve ser maior ou igual a zero."
    )
    BigDecimal valorConsultaParticular
) {}
