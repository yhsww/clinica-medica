package kyj.poo.clinica_medica.dto;

import kyj.poo.clinica_medica.model.Avaliacao;

public record AvaliacaoDTO(
    Long id,
    Long consultaId,
    Long medicoId,
    String medicoNome,
    String pacienteNome,
    Integer estrelas,
    String comentario
) {
    public static AvaliacaoDTO de(Avaliacao avaliacao) {
        return new AvaliacaoDTO(
            avaliacao.getId(),
            avaliacao.getConsulta() != null
                ? avaliacao.getConsulta().getId()
                : null,
            avaliacao.getMedico() != null ? avaliacao.getMedico().getId() : null,
            avaliacao.getMedico() != null
                ? avaliacao.getMedico().getNome()
                : null,
            avaliacao.getPaciente() != null
                ? avaliacao.getPaciente().getNome()
                : null,
            avaliacao.getEstrelas(),
            avaliacao.getComentario()
        );
    }
}
