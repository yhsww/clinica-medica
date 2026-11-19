package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Avaliacao {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne private Medico medico;

  @OneToOne
  @JoinColumn(unique = true)
  private Consulta consulta;

  @ManyToOne private Paciente paciente;

  private Integer estrelas;

  private String comentario;

  public Avaliacao(
      Medico medico, Paciente paciente, Consulta consulta, Integer estrelas, String comentario) {
    this.medico = medico;
    this.paciente = paciente;
    this.consulta = consulta;
    this.estrelas = estrelas;
    this.comentario = comentario;
  }
}
