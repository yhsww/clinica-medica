package kyj.poo.clinica_medica.dto;

import java.time.LocalDateTime;

public record NotificacaoDTO(
    Long id,
    String mensagem,
    LocalDateTime dataHora,
    boolean lida
) {}
