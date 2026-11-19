package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;

import kyj.poo.clinica_medica.model.especialidade.Especialidade;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Medico extends Usuario {

    @ManyToOne private Especialidade especialidade;

    private BigDecimal valorConsultaParticular;

    @ManyToMany
    @JoinTable(
            name = "medico_plano",
            joinColumns = @JoinColumn(name = "medico_id"),
            inverseJoinColumns = @JoinColumn(name = "plano_id"))
    private List<Plano> planosAceitos = new ArrayList<>();

    @OneToMany(mappedBy = "medico")
    private List<Avaliacao> avaliacoes = new ArrayList<>();

    public Medico(
            String nome,
            String email,
            String senha,
            Especialidade especialidade,
            BigDecimal valorConsultaParticular) {
        super(nome, email, senha);
        this.especialidade = especialidade;
        this.valorConsultaParticular = valorConsultaParticular;
    }

    public void adicionarPlanoAceito(Plano plano) {
        if (plano != null && !planosAceitos.contains(plano)) {
            planosAceitos.add(plano);
        }
    }

    public void removerPlanoAceito(Plano plano) {
        planosAceitos.remove(plano);
    }

    public boolean aceitaPlano(Plano plano) {
        return plano != null && planosAceitos.contains(plano);
    }
}
