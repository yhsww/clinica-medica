package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Paciente paciente;

    private String mensagem;

    private LocalDateTime dataHora;

    private boolean lida;

    public Notificacao() {}

    public Notificacao(
            Paciente paciente,
            String mensagem
    ) {
        this.paciente = paciente;
        this.mensagem = mensagem;
        this.dataHora = LocalDateTime.now();
        this.lida = false;
    }

    public Long getId() {
        return this.id;
    }

    public Paciente getPaciente() {
        return this.paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public String getMensagem() {
        return this.mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public LocalDateTime getDataHora() {
        return this.dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public boolean isLida() {
        return this.lida;
    }

    public void setLida(boolean lida) {
        this.lida = lida;
    }
}