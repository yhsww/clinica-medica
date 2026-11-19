package kyj.poo.clinica_medica.repository;

import kyj.poo.clinica_medica.model.Notificacao;
import kyj.poo.clinica_medica.model.Paciente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByPacienteOrderByDataHoraDesc(Paciente paciente);

    List<Notificacao> findByPacienteAndLidaFalseOrderByDataHoraDesc(Paciente paciente);
}
