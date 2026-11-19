package kyj.poo.clinica_medica.repository;

import kyj.poo.clinica_medica.model.Plano;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanoRepository extends JpaRepository<Plano, Long> {

    Optional<Plano> findByNomeIgnoreCase(String nome);
}
