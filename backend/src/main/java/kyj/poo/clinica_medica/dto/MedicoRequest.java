package kyj.poo.clinica_medica.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import kyj.poo.clinica_medica.model.PlanoNome;
import kyj.poo.clinica_medica.model.especialidade.EspecialidadeNome;

/**
 * planosAceitos é opcional: quando vier null, os planos atuais do médico
 * são mantidos.
 */
public record MedicoRequest(
    @NotBlank(message = "O nome é obrigatório.") String nome,
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "E-mail inválido.")
    String email,
    @NotNull(message = "A especialidade é obrigatória.")
    EspecialidadeNome especialidade,
    @NotNull(message = "O valor da consulta é obrigatório.")
    @DecimalMin(
        value = "0.00",
        message = "O valor deve ser maior ou igual a zero."
    )
    BigDecimal valorConsultaParticular,
    List<PlanoNome> planosAceitos
) {}
