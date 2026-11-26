package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ListaDeEspera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne private Medico medico;

    @ManyToOne private Paciente paciente;

    @Column(name = "data")
    private LocalDateTime dataHora;

    private Integer posicao;

    public ListaDeEspera(Medico medico, Paciente paciente, LocalDateTime dataHora, Integer posicao) {
        this.medico = medico;
        this.paciente = paciente;
        this.dataHora = dataHora;
        this.posicao = posicao;
    }
}
