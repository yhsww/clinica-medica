package kyj.poo.clinica_medica.repository;

import kyj.poo.clinica_medica.model.Credencial;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredencialRepository extends JpaRepository<Credencial, Long> {

    Optional<Credencial> findByUsuario_Email(String email);

    boolean existsByUsuario_Email(String email);
}
