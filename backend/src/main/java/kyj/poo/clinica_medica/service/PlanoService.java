package kyj.poo.clinica_medica.service;

import java.util.Arrays;
import java.util.List;
import kyj.poo.clinica_medica.model.PlanoNome;
import org.springframework.stereotype.Service;

@Service
public class PlanoService {

    public List<String> listarNomes() {
        return Arrays.stream(PlanoNome.values())
            .map(PlanoNome::getNome)
            .toList();
    }
}
