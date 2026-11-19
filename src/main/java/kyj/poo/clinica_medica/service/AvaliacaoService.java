package kyj.poo.clinica_medica.service;

import kyj.poo.clinica_medica.exception.OperacaoInvalidaException;
import kyj.poo.clinica_medica.exception.RecursoNaoEncontradoException;
import kyj.poo.clinica_medica.model.Avaliacao;
import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.EstadoDaConsulta;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.repository.AvaliacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvaliacaoService {

  private final AvaliacaoRepository avaliacaoRepository;

  public AvaliacaoService(AvaliacaoRepository avaliacaoRepository) {

    this.avaliacaoRepository = avaliacaoRepository;
  }

  @Transactional
  public Avaliacao avaliar(
      Consulta consulta, Paciente paciente, Integer estrelas, String comentario) {

    validarDados(consulta, paciente, estrelas);

    if (consulta.getEstado() != EstadoDaConsulta.REALIZADA) {

      throw new OperacaoInvalidaException("Somente consultas realizadas podem ser avaliadas.");
    }

    if (!consulta.getPaciente().equals(paciente)) {

      throw new OperacaoInvalidaException("O paciente informado não é o paciente da consulta.");
    }

    if (avaliacaoRepository.findByConsulta(consulta).isPresent()) {

      throw new OperacaoInvalidaException("Esta consulta já possui uma avaliação.");
    }

    Medico medico = consulta.getMedico();

    Avaliacao avaliacao = new Avaliacao(medico, paciente, consulta, estrelas, comentario);

    return avaliacaoRepository.save(avaliacao);
  }

  public Avaliacao buscarPorConsulta(Consulta consulta) {

    if (consulta == null) {
      throw new IllegalArgumentException("A consulta é obrigatória.");
    }

    return avaliacaoRepository
        .findByConsulta(consulta)
        .orElseThrow(
            () -> new RecursoNaoEncontradoException("Esta consulta ainda não possui avaliação."));
  }

  private void validarDados(Consulta consulta, Paciente paciente, Integer estrelas) {

    if (consulta == null) {
      throw new IllegalArgumentException("A consulta é obrigatória.");
    }

    if (paciente == null) {
      throw new IllegalArgumentException("O paciente é obrigatório.");
    }

    if (estrelas == null) {
      throw new IllegalArgumentException("A quantidade de estrelas é obrigatória.");
    }

    if (estrelas < 1 || estrelas > 5) {
      throw new IllegalArgumentException("A avaliação deve possuir entre 1 e 5 estrelas.");
    }
  }
}
