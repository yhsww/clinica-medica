package kyj.poo.clinica_medica.repository;

import java.util.Optional;
import kyj.poo.clinica_medica.model.Plano;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanoRepository extends JpaRepository<Plano, Long> {
    Optional<Plano> findByNomeIgnoreCase(String nome);
}
