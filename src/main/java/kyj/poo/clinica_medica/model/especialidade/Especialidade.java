package kyj.poo.clinica_medica.model.especialidade;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Especialidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    public Especialidade() {}

    public Especialidade(String nome) {
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public abstract int getLimiteConsultasPorDia();

    public abstract BigDecimal calcularValorConsulta(
            BigDecimal valorBase
    );
}