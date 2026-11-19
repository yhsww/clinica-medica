package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Paciente extends Usuario {

    private Integer idade;

    @ManyToOne private Plano planoSaude;

    @OneToMany(mappedBy = "paciente")
    private List<Consulta> consultas = new ArrayList<>();

    public Paciente(String nome, String email, Integer idade, Plano planoSaude) {
        super(nome, email);
        this.idade = idade;
        this.planoSaude = planoSaude;
    }
}
