package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Paciente extends Usuario {

  private Integer idade;

  @ManyToOne private Plano planoSaude;

  @OneToMany(mappedBy = "paciente")
  private List<Consulta> consultas = new ArrayList<>();

  public Paciente(String nome, String email, String senha, Integer idade, Plano planoSaude) {
    super(nome, email, senha);
    this.idade = idade;
    this.planoSaude = planoSaude;
  }
}
