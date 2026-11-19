package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "planos")
@Getter
@Setter
@NoArgsConstructor
public class Plano {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String nome;

  public Plano(String nome) {
    this.nome = nome;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof Plano)) {
      return false;
    }

    Plano outro = (Plano) o;

    return id != null && id.equals(outro.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
