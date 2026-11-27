package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Consulta consulta;

    private BigDecimal valor;

    private boolean paga;

    public Conta(Consulta consulta, BigDecimal valor) {
        this.consulta = consulta;
        this.valor = valor;
        this.paga = false;
    }
}
