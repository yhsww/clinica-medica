package kyj.poo.clinica_medica.repository;

import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.model.Plano;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByNome(String nome);

    Optional<Paciente> findByEmail(String email);

    List<Paciente> findByPlanoSaude(Plano plano);
}
