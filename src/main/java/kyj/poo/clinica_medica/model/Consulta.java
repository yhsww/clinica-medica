package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Medico medico;

    @ManyToOne
    private Paciente paciente;

    private LocalDate data;

    @Enumerated(EnumType.STRING)
    private EstadoDaConsulta estado;

    private BigDecimal valorCobrado;

    public Consulta() {}

    public Consulta(
            Medico medico,
            Paciente paciente,
            LocalDate data,
            BigDecimal valorCobrado
    ) {
        this.medico = medico;
        this.paciente = paciente;
        this.data = data;
        this.estado = EstadoDaConsulta.AGENDADA;
        this.valorCobrado = valorCobrado;
    }

    public Long getId() {
        return id;
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public EstadoDaConsulta getEstado() {
        return estado;
    }

    public void setEstado(EstadoDaConsulta estado) {
        this.estado = estado;
    }

    public BigDecimal getValorCobrado() {
        return valorCobrado;
    }

    public void setValorCobrado(BigDecimal valorCobrado) {
        this.valorCobrado = valorCobrado;
    }
}