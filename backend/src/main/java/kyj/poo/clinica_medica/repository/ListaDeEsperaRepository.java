package kyj.poo.clinica_medica.repository;

import java.time.LocalDateTime;
import java.util.List;
import kyj.poo.clinica_medica.model.ListaDeEspera;
import kyj.poo.clinica_medica.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListaDeEsperaRepository
    extends JpaRepository<ListaDeEspera, Long>
{
    List<ListaDeEspera> findByMedicoAndDataHoraGreaterThanEqualAndDataHoraLessThanOrderByPosicaoAsc(
        Medico medico,
        LocalDateTime inicio,
        LocalDateTime fim
    );
}
