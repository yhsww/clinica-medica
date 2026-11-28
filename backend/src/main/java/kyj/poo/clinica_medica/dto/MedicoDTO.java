package kyj.poo.clinica_medica.dto;

import java.math.BigDecimal;
import java.util.List;

public record MedicoDTO(
    Long id,
    String nome,
    String email,
    String especialidade,
    BigDecimal valorConsultaParticular,
    List<String> planosAceitos,
    double mediaEstrelas,
    List<AvaliacaoDTO> avaliacoesRecentes
) {}
