package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne private Consulta consulta;

    private BigDecimal valor;

    private boolean paga;

    public Conta(Consulta consulta, BigDecimal valor) {
        this.consulta = consulta;
        this.valor = valor;
        this.paga = false;
    }
}
