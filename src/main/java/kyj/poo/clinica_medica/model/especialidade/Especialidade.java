package kyj.poo.clinica_medica.model.especialidade;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public abstract class Especialidade {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String nome;

  public Especialidade(String nome) {
    this.nome = nome;
  }

  public abstract int getLimiteConsultasPorDia();

  public abstract BigDecimal calcularValorConsulta(BigDecimal valorBase);
}
