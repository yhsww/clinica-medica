package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;

@Entity
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

    public Prontuario() {}

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

    public Long getId() {
        return this.id;
    }

    public Consulta getConsulta() {
        return this.consulta;
    }

    public void setConsulta(Consulta consulta) {
        this.consulta = consulta;
    }

    public String getSintomas() {
        return this.sintomas;
    }

    public void setSintomas(String sintomas) {
        this.sintomas = sintomas;
    }

    public String getDiagnostico() {
        return this.diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getTratamento() {
        return this.tratamento;
    }

    public void setTratamento(String tratamento) {
        this.tratamento = tratamento;
    }

    public String getMedicamentos() {
        return this.medicamentos;
    }

    public void setMedicamentos(String medicamentos) {
        this.medicamentos = medicamentos;
    }

    public String getExames() {
        return this.exames;
    }

    public void setExames(String exames) {
        this.exames = exames;
    }

    public String getObservacoes() {
        return this.observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}