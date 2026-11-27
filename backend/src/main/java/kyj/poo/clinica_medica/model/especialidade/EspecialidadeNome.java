package kyj.poo.clinica_medica.model.especialidade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Optional;

public enum EspecialidadeNome {
    CARDIOLOGIA(new Cardiologia().getNome()),
    CLINICO_GERAL(new ClinicoGeral().getNome()),
    DERMATOLOGIA(new Dermatologia().getNome()),
    PEDIATRIA(new Pediatria().getNome());

    private final String nome;

    EspecialidadeNome(String nome) {
        this.nome = nome;
    }

    @JsonValue
    public String getNome() {
        return nome;
    }

    public static Optional<EspecialidadeNome> fromNome(String valor) {
        if (valor == null || valor.isBlank()) {
            return Optional.empty();
        }

        String nome = valor.trim();

        return Arrays.stream(values())
            .filter(especialidade -> especialidade.nome.equalsIgnoreCase(nome))
            .findFirst();
    }

    @JsonCreator
    public static EspecialidadeNome fromJson(String valor) {
        return fromNome(valor).orElseThrow(() ->
            new IllegalArgumentException("Especialidade inválida.")
        );
    }
}
