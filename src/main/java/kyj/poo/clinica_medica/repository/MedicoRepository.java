package kyj.poo.clinica_medica.repository;

import kyj.poo.clinica_medica.model.Especialidade;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Plano;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicoRepository extends JpaRepository<Medico, Long> {

    List<Medico> findByNomeContainingIgnoreCase(
            String nome
    );

    List<Medico> findByEspecialidade(
            Especialidade especialidade
    );

    List<Medico> findByNomeContainingIgnoreCaseAndEspecialidade(
            String nome,
            Especialidade especialidade
    );

    List<Medico> findByPlanosAceitosContaining(
            Plano plano
    );
}