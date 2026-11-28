package kyj.poo.clinica_medica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProntuarioRequest(
    @NotNull(message = "O ID da consulta é obrigatório.") Long consultaId,
    @NotBlank(message = "Os sintomas são obrigatórios.")
    @Size(max = 255, message = "Sintomas: máximo de 255 caracteres.")
    String sintomas,
    @NotBlank(message = "O diagnóstico é obrigatório.")
    @Size(max = 255, message = "Diagnóstico: máximo de 255 caracteres.")
    String diagnostico,
    @Size(max = 255, message = "Tratamento: máximo de 255 caracteres.")
    String tratamento,
    @Size(max = 255, message = "Medicamentos: máximo de 255 caracteres.")
    String medicamentos,
    @Size(max = 255, message = "Exames: máximo de 255 caracteres.")
    String exames,
    @Size(max = 255, message = "Observações: máximo de 255 caracteres.")
    String observacoes
) {}
