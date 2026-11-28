package kyj.poo.clinica_medica.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import kyj.poo.clinica_medica.exception.OperacaoInvalidaException;
import kyj.poo.clinica_medica.model.Avaliacao;
import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.EstadoDaConsulta;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.repository.AvaliacaoRepository;
import org.junit.jupiter.api.Test;

class AvaliacaoServiceTest {

    private final AvaliacaoRepository avaliacaoRepository = mock(
        AvaliacaoRepository.class
    );
    private final AvaliacaoService service = new AvaliacaoService(
        avaliacaoRepository
    );

    private Consulta consultaRealizada(Paciente paciente) {
        Consulta consulta = new Consulta(
            new Medico(),
            paciente,
            LocalDate.now().atTime(9, 0),
            BigDecimal.TEN
        );
        consulta.setEstado(EstadoDaConsulta.REALIZADA);
        return consulta;
    }

    @Test
    void naoDevePermitirAvaliacaoDuplicada() {
        Paciente paciente = new Paciente();
        Consulta consulta = consultaRealizada(paciente);

        when(avaliacaoRepository.findByConsulta(consulta)).thenReturn(
            Optional.of(new Avaliacao())
        );

        assertThrows(OperacaoInvalidaException.class, () ->
            service.avaliar(consulta, paciente, 5, "Ótimo atendimento")
        );
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void naoDeveAvaliarConsultaNaoRealizada() {
        Paciente paciente = new Paciente();
        Consulta consulta = consultaRealizada(paciente);
        consulta.setEstado(EstadoDaConsulta.AGENDADA);

        assertThrows(OperacaoInvalidaException.class, () ->
            service.avaliar(consulta, paciente, 4, "ok")
        );
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void naoDeveAceitarNotaForaDoIntervalo() {
        Paciente paciente = new Paciente();
        Consulta consulta = consultaRealizada(paciente);

        assertThrows(IllegalArgumentException.class, () ->
            service.avaliar(consulta, paciente, 6, "nota inválida")
        );
    }
}
