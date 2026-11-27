package kyj.poo.clinica_medica.model.especialidade;

import jakarta.persistence.Entity;
import java.math.BigDecimal;

@Entity
public class Cardiologia extends Especialidade {

    public Cardiologia() {
        super("Cardiologia");
    }

    @Override
    public int getLimiteConsultasPorDia() {
        return 3;
    }

    @Override
    public BigDecimal calcularValorConsulta(BigDecimal valorBase) {
        return valorBase.multiply(BigDecimal.valueOf(1.20));
    }
}
