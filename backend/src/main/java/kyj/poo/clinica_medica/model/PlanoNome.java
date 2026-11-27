package kyj.poo.clinica_medica.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Optional;

public enum PlanoNome {
    SUS("SUS"),
    PARTICULAR("Particular");

    private final String nome;

    PlanoNome(String nome) {
        this.nome = nome;
    }

    @JsonValue
    public String getNome() {
        return nome;
    }

    public static Optional<PlanoNome> fromNome(String valor) {
        if (valor == null || valor.isBlank()) {
            return Optional.empty();
        }

        String nome = valor.trim();

        return Arrays.stream(values())
            .filter(plano -> plano.nome.equalsIgnoreCase(nome))
            .findFirst();
    }

    @JsonCreator
    public static PlanoNome fromJson(String valor) {
        return fromNome(valor).orElseThrow(() ->
            new IllegalArgumentException("Plano de saúde inválido.")
        );
    }
}
