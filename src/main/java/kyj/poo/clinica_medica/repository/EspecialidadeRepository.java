package kyj.poo.clinica_medica.repository;

import kyj.poo.clinica_medica.model.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EspecialidadeRepository extends JpaRepository<Especialidade, Long> {

    Optional<Especialidade> findByNomeIgnoreCase(String nome);
}