package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Notificacao {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne private Paciente paciente;

  private String mensagem;

  private LocalDateTime dataHora;

  private boolean lida;

  public Notificacao(Paciente paciente, String mensagem) {
    this.paciente = paciente;
    this.mensagem = mensagem;
    this.dataHora = LocalDateTime.now();
    this.lida = false;
  }
}
