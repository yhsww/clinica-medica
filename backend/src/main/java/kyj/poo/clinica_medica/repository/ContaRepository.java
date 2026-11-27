package kyj.poo.clinica_medica.repository;

import java.util.List;
import java.util.Optional;
import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.Conta;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContaRepository extends JpaRepository<Conta, Long> {
    Optional<Conta> findByConsulta(Consulta consulta);

    List<Conta> findByConsulta_Paciente(Paciente paciente);

    List<Conta> findByConsulta_Medico(Medico medico);

    List<Conta> findByPagaFalse();

    List<Conta> findByPagaTrue();
}
