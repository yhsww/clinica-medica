package kyj.poo.clinica_medica.repository;

import java.util.Optional;
import kyj.poo.clinica_medica.model.Avaliacao;
import kyj.poo.clinica_medica.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

  Optional<Avaliacao> findByConsulta(Consulta consulta);
}
