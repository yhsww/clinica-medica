package kyj.poo.clinica_medica.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import kyj.poo.clinica_medica.exception.AgendamentoIndisponivelException;
import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.EstadoDaConsulta;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.model.especialidade.Especialidade;
import kyj.poo.clinica_medica.repository.ConsultaRepository;
import kyj.poo.clinica_medica.repository.ListaDeEsperaRepository;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

class AgendamentoServiceTest {

    private final ConsultaRepository consultaRepository = mock(ConsultaRepository.class);
    private final ListaDeEsperaRepository listaRepository = mock(ListaDeEsperaRepository.class);
    private final AgendamentoService service =
            new AgendamentoService(consultaRepository, listaRepository);

    @Test
    void deveAdicionarPacienteNaFilaQuandoAgendaEstiverCheia() {
        Medico medico = mock(Medico.class);
        Paciente paciente = mock(Paciente.class);
        Especialidade especialidade = mock(Especialidade.class);
        Consulta consulta1 = mock(Consulta.class);
        Consulta consulta2 = mock(Consulta.class);

        LocalDate data = LocalDate.now().plusDays(1);

        when(medico.getEspecialidade()).thenReturn(especialidade);
        when(especialidade.getLimiteConsultasPorDia()).thenReturn(2);
        when(medico.getValorConsultaParticular()).thenReturn(new BigDecimal("200.00"));
        when(consultaRepository.findByMedicoAndPacienteAndData(medico, paciente, data))
                .thenReturn(Optional.empty());
        when(consulta1.getEstado()).thenReturn(EstadoDaConsulta.AGENDADA);
        when(consulta2.getEstado()).thenReturn(EstadoDaConsulta.AGENDADA);
        when(consultaRepository.findByMedicoAndData(medico, data))
                .thenReturn(List.of(consulta1, consulta2));
        when(listaRepository.findByMedicoAndDataOrderByPosicaoAsc(medico, data))
                .thenReturn(List.of());

        assertThrows(
                AgendamentoIndisponivelException.class,
                () -> service.agendar(medico, paciente, data));

        verify(listaRepository).save(any());
    }
}
