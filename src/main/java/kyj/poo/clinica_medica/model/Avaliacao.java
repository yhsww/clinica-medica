package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;

@Entity
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Medico medico;

    @OneToOne
    @JoinColumn(unique = true)
    private Consulta consulta;

    @ManyToOne
    private Paciente paciente;

    private Integer estrelas;

    private String comentario;

    public Avaliacao() {}

    public Avaliacao(
            Medico medico,
            Paciente paciente,
            Consulta consulta,
            Integer estrelas,
            String comentario
    ) {
        this.medico = medico;
        this.paciente = paciente;
        this.consulta = consulta;
        this.estrelas = estrelas;
        this.comentario = comentario;
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

    public Consulta getConsulta() {
        return this.consulta;
    }

    public void setConsulta(Consulta consulta) {
        this.consulta = consulta;
    }

    public Integer getEstrelas() {
        return this.estrelas;
    }

    public void setEstrelas(Integer estrelas) {
        this.estrelas = estrelas;
    }

    public String getComentario() {
        return this.comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}