package kyj.poo.clinica_medica.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Credencial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Papel papel;

    @Column(nullable = false)
    private boolean ativo = true;

    public Credencial(Usuario usuario, String senha, Papel papel) {
        this.usuario = usuario;
        this.senha = senha;
        this.papel = papel;
        this.ativo = true;
    }
}
