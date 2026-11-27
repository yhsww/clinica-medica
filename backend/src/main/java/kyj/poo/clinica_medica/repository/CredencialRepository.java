package kyj.poo.clinica_medica.repository;

import java.util.Optional;
import kyj.poo.clinica_medica.model.Credencial;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredencialRepository extends JpaRepository<Credencial, Long> {
    Optional<Credencial> findByUsuario_Email(String email);

    Optional<Credencial> findByUsuario_Id(Long usuarioId);

    boolean existsByUsuario_Email(String email);
}
