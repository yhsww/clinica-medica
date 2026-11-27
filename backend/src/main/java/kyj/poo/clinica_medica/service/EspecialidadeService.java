package kyj.poo.clinica_medica.service;

import java.util.Arrays;
import java.util.List;
import kyj.poo.clinica_medica.model.especialidade.EspecialidadeNome;
import org.springframework.stereotype.Service;

@Service
public class EspecialidadeService {

    public List<String> listarNomes() {
        return Arrays.stream(EspecialidadeNome.values())
            .map(EspecialidadeNome::getNome)
            .toList();
    }
}
