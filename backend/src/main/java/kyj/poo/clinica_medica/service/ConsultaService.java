package kyj.poo.clinica_medica.service;

import kyj.poo.clinica_medica.exception.OperacaoInvalidaException;
import kyj.poo.clinica_medica.exception.RecursoNaoEncontradoException;
import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.EstadoDaConsulta;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.repository.ConsultaRepository;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final ListaDeEsperaService listaDeEsperaService;

    public ConsultaService(
            ConsultaRepository consultaRepository, ListaDeEsperaService listaDeEsperaService) {

        this.consultaRepository = consultaRepository;
        this.listaDeEsperaService = listaDeEsperaService;
    }

    @Transactional
    public Consulta cancelar(Long consultaId) {

        Consulta consulta = buscarPorId(consultaId);

        if (consulta.getEstado() != EstadoDaConsulta.AGENDADA) {
            throw new OperacaoInvalidaException(
                    "Somente consultas agendadas podem ser canceladas.");
        }

        consulta.setEstado(EstadoDaConsulta.CANCELADA);

        Consulta consultaCancelada = consultaRepository.save(consulta);

        listaDeEsperaService.promoverPrimeiroDaFila(consulta.getMedico(), consulta.getDataHora().toLocalDate());

        return consultaCancelada;
    }

    @Transactional
    public Consulta realizar(Long consultaId) {

        Consulta consulta = buscarPorId(consultaId);

        if (consulta.getEstado() != EstadoDaConsulta.AGENDADA) {
            throw new OperacaoInvalidaException(
                    "Somente consultas agendadas podem ser realizadas.");
        }

        consulta.setEstado(EstadoDaConsulta.REALIZADA);

        return consultaRepository.save(consulta);
    }

    public Consulta buscarPorId(Long consultaId) {

        if (consultaId == null) {
            throw new IllegalArgumentException("O ID da consulta é obrigatório.");
        }

        return consultaRepository
                .findById(consultaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada."));
    }

    public boolean pertenceAoPaciente(Consulta consulta, Paciente paciente) {

        if (consulta == null || paciente == null) {
            return false;
        }

        return consulta.getPaciente().equals(paciente);
    }

    public boolean pertenceAoMedico(Consulta consulta, Medico medico) {

        if (consulta == null || medico == null) {
            return false;
        }

        return consulta.getMedico().equals(medico);
    }
}
