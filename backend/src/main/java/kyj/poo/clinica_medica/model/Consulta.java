package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne private Medico medico;

    @ManyToOne private Paciente paciente;

    @Column(name = "data")
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    private EstadoDaConsulta estado;

    private BigDecimal valorCobrado;

    public Consulta(Medico medico, Paciente paciente, LocalDateTime dataHora, BigDecimal valorCobrado) {
        this.medico = medico;
        this.paciente = paciente;
        this.dataHora = dataHora;
        this.estado = EstadoDaConsulta.AGENDADA;
        this.valorCobrado = valorCobrado;
    }
}
