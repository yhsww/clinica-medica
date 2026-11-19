package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Paciente extends Usuario {

    private Integer idade;

    @ManyToOne
    private Plano planoSaude;

    @OneToMany(mappedBy = "paciente")
    private List<Consulta> consultas = new ArrayList<>();

    public Paciente() {}

    public Paciente(
            String nome,
            String email,
            String senha,
            Integer idade,
            Plano planoSaude
    ) {
        super(nome, email, senha);
        this.idade = idade;
        this.planoSaude = planoSaude;
    }

    public Integer getIdade() {
        return this.idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    public Plano getPlanoSaude() {
        return this.planoSaude;
    }

    public void setPlanoSaude(Plano planoSaude) {
        this.planoSaude = planoSaude;
    }

    public List<Consulta> getConsultas() {
        return this.consultas;
    }

    public void setConsultas(List<Consulta> consultas) {
        this.consultas = consultas;
    }
}