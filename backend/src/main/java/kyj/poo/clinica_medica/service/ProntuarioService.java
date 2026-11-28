package kyj.poo.clinica_medica.service;

import java.util.Comparator;
import java.util.List;
import kyj.poo.clinica_medica.exception.AcessoNegadoException;
import kyj.poo.clinica_medica.exception.OperacaoInvalidaException;
import kyj.poo.clinica_medica.exception.RecursoNaoEncontradoException;
import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.EstadoDaConsulta;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.model.Prontuario;
import kyj.poo.clinica_medica.repository.ProntuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProntuarioService {

    private final ProntuarioRepository prontuarioRepository;

    public ProntuarioService(ProntuarioRepository prontuarioRepository) {
        this.prontuarioRepository = prontuarioRepository;
    }

    @Transactional
    public Prontuario criar(
        Consulta consulta,
        String sintomas,
        String diagnostico,
        String tratamento,
        String medicamentos,
        String exames,
        String observacoes
    ) {
        if (consulta == null) {
            throw new IllegalArgumentException("A consulta é obrigatória.");
        }

        if (consulta.getEstado() != EstadoDaConsulta.REALIZADA) {
            throw new OperacaoInvalidaException(
                "O prontuário só pode ser registrado após a realização da consulta."
            );
        }

        if (prontuarioRepository.findByConsulta(consulta).isPresent()) {
            throw new OperacaoInvalidaException(
                "Esta consulta já possui um prontuário."
            );
        }

        Prontuario prontuario = new Prontuario(
            consulta,
            sintomas,
            diagnostico,
            tratamento,
            medicamentos,
            exames,
            observacoes
        );

        return prontuarioRepository.save(prontuario);
    }

    public Prontuario buscarPorConsulta(Consulta consulta) {
        if (consulta == null) {
            throw new IllegalArgumentException("A consulta é obrigatória.");
        }

        return prontuarioRepository
            .findByConsulta(consulta)
            .orElseThrow(() ->
                new RecursoNaoEncontradoException(
                    "Prontuário não encontrado para esta consulta."
                )
            );
    }

    /** Histórico do paciente, da consulta mais recente para a mais antiga. */
    public List<Prontuario> listarHistorico(Paciente paciente) {
        if (paciente == null) {
            throw new IllegalArgumentException("O paciente é obrigatório.");
        }

        return prontuarioRepository
            .findByConsulta_Paciente(paciente)
            .stream()
            .sorted(
                Comparator.comparing((Prontuario p) ->
                    p.getConsulta().getDataHora()
                ).reversed()
            )
            .toList();
    }

    public Prontuario buscarParaPaciente(Consulta consulta, Paciente paciente) {
        if (consulta == null || paciente == null) {
            throw new IllegalArgumentException(
                "A consulta e o paciente são obrigatórios."
            );
        }

        if (!consulta.getPaciente().equals(paciente)) {
            throw new AcessoNegadoException(
                "O paciente não possui acesso a este prontuário."
            );
        }

        return buscarPorConsulta(consulta);
    }

    public Prontuario buscarParaMedico(Consulta consulta, Medico medico) {
        if (consulta == null || medico == null) {
            throw new IllegalArgumentException(
                "A consulta e o médico são obrigatórios."
            );
        }

        if (!consulta.getMedico().equals(medico)) {
            throw new AcessoNegadoException(
                "O médico não possui autorização para acessar este prontuário."
            );
        }

        return buscarPorConsulta(consulta);
    }

    public boolean existeParaConsulta(Consulta consulta) {
        if (consulta == null) {
            return false;
        }

        return prontuarioRepository.findByConsulta(consulta).isPresent();
    }
}
