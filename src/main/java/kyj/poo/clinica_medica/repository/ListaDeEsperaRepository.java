package kyj.poo.clinica_medica.repository;

import java.time.LocalDate;
import java.util.List;
import kyj.poo.clinica_medica.model.ListaDeEspera;
import kyj.poo.clinica_medica.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListaDeEsperaRepository extends JpaRepository<ListaDeEspera, Long> {

  List<ListaDeEspera> findByMedicoAndDataOrderByPosicaoAsc(Medico medico, LocalDate data);
}
