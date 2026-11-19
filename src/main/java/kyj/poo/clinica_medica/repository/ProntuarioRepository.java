package kyj.poo.clinica_medica.repository;

import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.Prontuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProntuarioRepository extends JpaRepository<Prontuario, Long> {

    Optional<Prontuario> findByConsulta(Consulta consulta);
}