package kyj.poo.clinica_medica.model.especialidade;

import jakarta.persistence.Entity;
import java.math.BigDecimal;

@Entity
public class Pediatria extends Especialidade {

  public Pediatria() {
    super("Pediatria");
  }

  @Override
  public int getLimiteConsultasPorDia() {
    return 2;
  }

  @Override
  public BigDecimal calcularValorConsulta(BigDecimal valorBase) {
    return valorBase;
  }
}
