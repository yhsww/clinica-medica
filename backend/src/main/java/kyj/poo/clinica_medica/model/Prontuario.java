package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Prontuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Consulta consulta;

    private String sintomas;

    private String diagnostico;

    private String tratamento;

    private String medicamentos;

    private String exames;

    private String observacoes;

    public Prontuario(
        Consulta consulta,
        String sintomas,
        String diagnostico,
        String tratamento,
        String medicamentos,
        String exames,
        String observacoes
    ) {
        this.consulta = consulta;
        this.sintomas = sintomas;
        this.diagnostico = diagnostico;
        this.tratamento = tratamento;
        this.medicamentos = medicamentos;
        this.exames = exames;
        this.observacoes = observacoes;
    }
}
