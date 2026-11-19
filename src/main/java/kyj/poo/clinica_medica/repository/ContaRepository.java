package kyj.poo.clinica_medica.repository;

import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.Conta;
import kyj.poo.clinica_medica.model.Paciente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    Optional<Conta> findByConsulta(Consulta consulta);

    List<Conta> findByConsulta_Paciente(Paciente paciente);
}
