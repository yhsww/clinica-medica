package kyj.poo.clinica_medica.repository;

import java.util.List;
import kyj.poo.clinica_medica.model.Notificacao;
import kyj.poo.clinica_medica.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

  List<Notificacao> findByPacienteOrderByDataHoraDesc(Paciente paciente);

  List<Notificacao> findByPacienteAndLidaFalseOrderByDataHoraDesc(Paciente paciente);
}
