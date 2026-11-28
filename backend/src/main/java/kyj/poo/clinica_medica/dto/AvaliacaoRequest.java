package kyj.poo.clinica_medica.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvaliacaoRequest(
    @NotNull(message = "O ID da consulta é obrigatório.") Long consultaId,
    @NotNull(message = "A quantidade de estrelas é obrigatória.")
    @Min(value = 1, message = "A nota mínima é 1 estrela.")
    @Max(value = 5, message = "A nota máxima é 5 estrelas.")
    Integer estrelas,
    @Size(
        max = 255,
        message = "O comentário deve ter no máximo 255 caracteres."
    )
    String comentario
) {}
