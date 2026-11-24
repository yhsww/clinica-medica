package kyj.poo.clinica_medica.dto;

import java.util.List;

public record ErroResponse(
        int status,
        String erro,
        String mensagem,
        List<CampoErro> campos
) {
    public record CampoErro(String campo, String mensagem) {}
}
