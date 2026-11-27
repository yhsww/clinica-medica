package kyj.poo.clinica_medica.repository;

import java.util.Optional;
import kyj.poo.clinica_medica.model.especialidade.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecialidadeRepository
    extends JpaRepository<Especialidade, Long>
{
    Optional<Especialidade> findByNomeIgnoreCase(String nome);
}
