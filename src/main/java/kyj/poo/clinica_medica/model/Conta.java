package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Consulta consulta;

    private BigDecimal valor;

    private boolean paga;

    public Conta() {}

    public Conta(Consulta consulta, BigDecimal valor) {
        this.consulta = consulta;
        this.valor = valor;
        this.paga = false;
    }

    public Long getId() {
        return this.id;
    }

    public Consulta getConsulta() {
        return this.consulta;
    }

    public void setConsulta(Consulta consulta) {
        this.consulta = consulta;
    }

    public BigDecimal getValor() {
        return this.valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public boolean isPaga() {
        return this.paga;
    }

    public void setPaga(boolean paga) {
        this.paga = paga;
    }
}