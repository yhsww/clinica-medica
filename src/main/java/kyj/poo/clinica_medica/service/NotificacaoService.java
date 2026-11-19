package kyj.poo.clinica_medica.service;

import kyj.poo.clinica_medica.exception.RecursoNaoEncontradoException;
import kyj.poo.clinica_medica.model.Notificacao;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.repository.NotificacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoService(
            NotificacaoRepository notificacaoRepository) {

        this.notificacaoRepository = notificacaoRepository;
    }

    @Transactional
    public Notificacao criar(
            Paciente paciente,
            String mensagem) {

        if (paciente == null) {
            throw new IllegalArgumentException(
                    "O paciente é obrigatório."
            );
        }

        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException(
                    "A mensagem da notificação é obrigatória."
            );
        }

        Notificacao notificacao =
                new Notificacao(
                        paciente,
                        mensagem);

        return notificacaoRepository.save(notificacao);
    }

    public List<Notificacao> listarPorPaciente(
            Paciente paciente) {

        if (paciente == null) {
            throw new IllegalArgumentException(
                    "O paciente é obrigatório."
            );
        }

        return notificacaoRepository
                .findByPacienteOrderByDataHoraDesc(paciente);
    }

    public List<Notificacao> listarNaoLidas(
            Paciente paciente) {

        if (paciente == null) {
            throw new IllegalArgumentException(
                    "O paciente é obrigatório."
            );
        }

        return notificacaoRepository
                .findByPacienteAndLidaFalseOrderByDataHoraDesc(
                        paciente);
    }

    @Transactional
    public Notificacao marcarComoLida(
            Long notificacaoId) {

        Notificacao notificacao =
                buscarPorId(notificacaoId);

        notificacao.setLida(true);

        return notificacaoRepository.save(notificacao);
    }

    public Notificacao buscarPorId(
            Long notificacaoId) {

        if (notificacaoId == null) {
            throw new IllegalArgumentException(
                    "O ID da notificação é obrigatório."
            );
        }

        return notificacaoRepository
                .findById(notificacaoId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Notificação não encontrada."
                        )
                );
    }
}