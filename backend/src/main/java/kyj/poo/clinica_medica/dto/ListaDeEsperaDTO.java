package kyj.poo.clinica_medica.dto;

import java.time.LocalDateTime;

public record ListaDeEsperaDTO(
    Long id,
    Long medicoId,
    String medicoNome,
    Long pacienteId,
    String pacienteNome,
    LocalDateTime dataHora,
    Integer posicao
) {}
