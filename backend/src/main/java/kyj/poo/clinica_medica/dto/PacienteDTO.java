package kyj.poo.clinica_medica.dto;

public record PacienteDTO(
        Long id,
        String nome,
        String email,
        Integer idade,
        String planoSaude
) {}
