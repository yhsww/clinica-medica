package kyj.poo.clinica_medica.repository;

import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Paciente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByMedicoAndDataHoraGreaterThanEqualAndDataHoraLessThan(
            Medico medico, LocalDateTime inicio, LocalDateTime fim);

    Optional<Consulta> findByMedicoAndPacienteAndDataHoraGreaterThanEqualAndDataHoraLessThan(
            Medico medico, Paciente paciente, LocalDateTime inicio, LocalDateTime fim);

    boolean existsByMedico_Id(Long medicoId);

    boolean existsByPaciente_Id(Long pacienteId);
}
