package kyj.poo.clinica_medica.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
        ).thenReturn(List.of());
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

    private Medico medicoComLimite(int limite) {
        Medico medico = mock(Medico.class);
        Especialidade especialidade = mock(Especialidade.class);
        when(medico.getEspecialidade()).thenReturn(especialidade);
        when(especialidade.getLimiteConsultasPorDia()).thenReturn(limite);
        when(medico.getValorConsultaParticular()).thenReturn(
            new BigDecimal("200.00")
        );
        return medico;
    }

    private Consulta consultaAgendada(Long id, Medico medico, Paciente paciente, LocalDateTime dataHora) {
        Consulta consulta = new Consulta(medico, paciente, dataHora, BigDecimal.TEN);
        consulta.setId(id);
        return consulta;
    }

    @Test
    void remarcarNaoDeveContarAPropriaConsultaNoLimite() {
        Medico medico = medicoComLimite(1);
        Paciente paciente = mock(Paciente.class);
        LocalDateTime dataHora = LocalDate.now().plusDays(1).atTime(9, 0);
        Consulta consulta = consultaAgendada(1L, medico, paciente, dataHora);

        when(
            consultaRepository.findByMedicoAndDataHoraGreaterThanEqualAndDataHoraLessThan(
                eq(medico), any(), any()
            )
        ).thenReturn(List.of(consulta));
        when(
            consultaRepository.findByMedicoAndPacienteAndDataHoraGreaterThanEqualAndDataHoraLessThan(
                eq(medico), eq(paciente), any(), any()
            )
        ).thenReturn(List.of(consulta));

        service.remarcar(consulta, medico, paciente, dataHora.plusHours(2));

        verify(consultaRepository).save(consulta);
    }

    @Test
    void remarcarDeveRecusarDiaLotadoSemUsarListaDeEspera() {
        Medico medico = medicoComLimite(1);
        Paciente paciente = mock(Paciente.class);
        Paciente outroPaciente = mock(Paciente.class);
        LocalDateTime dataHora = LocalDate.now().plusDays(1).atTime(9, 0);
        Consulta consulta = consultaAgendada(1L, medico, paciente, dataHora);
        Consulta outra = consultaAgendada(2L, medico, outroPaciente, dataHora.plusDays(1));

        when(
            consultaRepository.findByMedicoAndDataHoraGreaterThanEqualAndDataHoraLessThan(
                eq(medico), any(), any()
            )
        ).thenReturn(List.of(outra));
        when(
            consultaRepository.findByMedicoAndPacienteAndDataHoraGreaterThanEqualAndDataHoraLessThan(
                eq(medico), eq(paciente), any(), any()
            )
        ).thenReturn(List.of());

        assertThrows(AgendamentoIndisponivelException.class, () ->
            service.remarcar(consulta, medico, paciente, dataHora.plusDays(1))
        );
        verify(consultaRepository, never()).save(any());
        verify(listaRepository, never()).save(any());
    }

    @Test
    void consultaCanceladaNoMesmoDiaNaoImpedeNovoAgendamento() {
        Medico medico = medicoComLimite(3);
        Paciente paciente = mock(Paciente.class);
        LocalDateTime dataHora = LocalDate.now().plusDays(1).atTime(9, 0);
        Consulta cancelada = consultaAgendada(1L, medico, paciente, dataHora);
        cancelada.setEstado(EstadoDaConsulta.CANCELADA);

        when(
            consultaRepository.findByMedicoAndPacienteAndDataHoraGreaterThanEqualAndDataHoraLessThan(
                eq(medico), eq(paciente), any(), any()
            )
        ).thenReturn(List.of(cancelada));
        when(
            consultaRepository.findByMedicoAndDataHoraGreaterThanEqualAndDataHoraLessThan(
                eq(medico), any(), any()
            )
        ).thenReturn(List.of(cancelada));

        service.agendar(medico, paciente, dataHora);

        verify(consultaRepository).save(any(Consulta.class));
    }
}
