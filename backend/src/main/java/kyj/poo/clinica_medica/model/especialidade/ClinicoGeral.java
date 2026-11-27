package kyj.poo.clinica_medica.model.especialidade;

import jakarta.persistence.Entity;
import java.math.BigDecimal;

@Entity
public class ClinicoGeral extends Especialidade {

    public ClinicoGeral() {
        super("Clínico Geral");
    }

    @Override
    public int getLimiteConsultasPorDia() {
        return 3;
    }

    @Override
    public BigDecimal calcularValorConsulta(BigDecimal valorBase) {
        return valorBase;
    }
}
