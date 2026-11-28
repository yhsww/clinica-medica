package kyj.poo.clinica_medica.repository;

import java.util.List;
import java.util.Optional;
import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.model.Prontuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProntuarioRepository extends JpaRepository<Prontuario, Long> {
    Optional<Prontuario> findByConsulta(Consulta consulta);

    List<Prontuario> findByConsulta_Paciente(Paciente paciente);
}
