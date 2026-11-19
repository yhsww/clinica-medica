package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    private LocalDate data;

    @Enumerated(EnumType.STRING)
    private EstadoDaConsulta estado;

    private BigDecimal valorCobrado;

    public Consulta(Medico medico, Paciente paciente, LocalDate data, BigDecimal valorCobrado) {
        this.medico = medico;
        this.paciente = paciente;
        this.data = data;
        this.estado = EstadoDaConsulta.AGENDADA;
        this.valorCobrado = valorCobrado;
    }
}
