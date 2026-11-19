package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class ListaDeEspera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Medico medico;

    @ManyToOne
    private Paciente paciente;

    private LocalDate data;

    private Integer posicao;

    public ListaDeEspera() {}

    public ListaDeEspera(
            Medico medico,
            Paciente paciente,
            LocalDate data,
            Integer posicao
    ) {
        this.medico = medico;
        this.paciente = paciente;
        this.data = data;
        this.posicao = posicao;
    }

    public Long getId() {
        return this.id;
    }

    public Medico getMedico() {
        return this.medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    public Paciente getPaciente() {
        return this.paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public LocalDate getData() {
        return this.data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public Integer getPosicao() {
        return this.posicao;
    }

    public void setPosicao(Integer posicao) {
        this.posicao = posicao;
    }
}