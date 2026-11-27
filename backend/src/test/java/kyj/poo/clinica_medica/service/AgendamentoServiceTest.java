package kyj.poo.clinica_medica.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kyj.poo.clinica_medica.exception.AgendamentoIndisponivelException;
import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.EstadoDaConsulta;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.model.especialidade.Especialidade;
import kyj.poo.clinica_medica.repository.ConsultaRepository;
import kyj.poo.clinica_medica.repository.ListaDeEsperaRepository;
import org.junit.jupiter.api.Test;

class AgendamentoServiceTest {

    private final ConsultaRepository consultaRepository = mock(
        ConsultaRepository.class
    );
    private final ListaDeEsperaRepository listaRepository = mock(
        ListaDeEsperaRepository.class
    );
    private final AgendamentoService service = new AgendamentoService(
        consultaRepository,
        listaRepository
    );

    @Test
    void deveAdicionarPacienteNaFilaQuandoAgendaEstiverCheia() {
        Medico medico = mock(Medico.class);
        Paciente paciente = mock(Paciente.class);
        Especialidade especialidade = mock(Especialidade.class);
        Consulta consulta1 = mock(Consulta.class);
        Consulta consulta2 = mock(Consulta.class);

        LocalDateTime dataHora = LocalDate.now().plusDays(1).atTime(10, 30);
        LocalDate data = dataHora.toLocalDate();
        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.plusDays(1).atStartOfDay();

        when(medico.getEspecialidade()).thenReturn(especialidade);
        when(especialidade.getLimiteConsultasPorDia()).thenReturn(2);
        when(medico.getValorConsultaParticular()).thenReturn(
            new BigDecimal("200.00")
        );
        when(
            consultaRepository.findByMedicoAndPacienteAndDataHoraGreaterThanEqualAndDataHoraLessThan(
                medico,
                paciente,
                inicio,
                fim
            )
        ).thenReturn(Optional.empty());
        when(consulta1.getEstado()).thenReturn(EstadoDaConsulta.AGENDADA);
        when(consulta2.getEstado()).thenReturn(EstadoDaConsulta.AGENDADA);
        when(
            consultaRepository.findByMedicoAndDataHoraGreaterThanEqualAndDataHoraLessThan(
                medico,
                inicio,
                fim
            )
        ).thenReturn(List.of(consulta1, consulta2));
        when(
            listaRepository.findByMedicoAndDataHoraGreaterThanEqualAndDataHoraLessThanOrderByPosicaoAsc(
                medico,
                inicio,
                fim
            )
        ).thenReturn(List.of());

        assertThrows(AgendamentoIndisponivelException.class, () ->
            service.agendar(medico, paciente, dataHora)
        );

        verify(listaRepository).save(any());
    }
}
