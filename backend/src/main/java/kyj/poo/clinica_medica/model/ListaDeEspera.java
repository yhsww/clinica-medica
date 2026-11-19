package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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

    private LocalDate data;

    private Integer posicao;

    public ListaDeEspera(Medico medico, Paciente paciente, LocalDate data, Integer posicao) {
        this.medico = medico;
        this.paciente = paciente;
        this.data = data;
        this.posicao = posicao;
    }
}
