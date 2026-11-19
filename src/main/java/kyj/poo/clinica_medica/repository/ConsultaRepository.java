package kyj.poo.clinica_medica.repository;

import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Paciente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByMedicoAndData(Medico medico, LocalDate data);

    Optional<Consulta> findByMedicoAndPacienteAndData(
            Medico medico, Paciente paciente, LocalDate data);
}
