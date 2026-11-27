package kyj.poo.clinica_medica.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import kyj.poo.clinica_medica.dto.ConsultaRequest;
import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.Conta;
import kyj.poo.clinica_medica.model.EstadoDaConsulta;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.repository.ConsultaRepository;
import kyj.poo.clinica_medica.repository.ContaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ConsultaServiceTest {

    private final ConsultaRepository consultaRepository = mock(
        ConsultaRepository.class
    );
    private final ListaDeEsperaService listaDeEsperaService = mock(
        ListaDeEsperaService.class
    );
    private final MedicoService medicoService = mock(MedicoService.class);
    private final PacienteService pacienteService = mock(PacienteService.class);
    private final AgendamentoService agendamentoService = mock(
        AgendamentoService.class
    );
    private final ContaRepository contaRepository = mock(ContaRepository.class);
    private final ConsultaService service = new ConsultaService(
        consultaRepository,
        listaDeEsperaService,
        medicoService,
        pacienteService,
        agendamentoService,
        contaRepository
    );

    @Test
    void deveCancelarConsultaEPromoverPrimeiroDaFila() {
        Long consultaId = 1L;
        Medico medico = mock(Medico.class);
        Consulta consulta = mock(Consulta.class);
        LocalDateTime dataHora = LocalDate.now().plusDays(1).atTime(10, 30);

        when(consulta.getEstado()).thenReturn(EstadoDaConsulta.AGENDADA);
        when(consulta.getMedico()).thenReturn(medico);
        when(consulta.getDataHora()).thenReturn(dataHora);
        when(consultaRepository.findById(consultaId)).thenReturn(
            Optional.of(consulta)
        );
        when(consultaRepository.save(consulta)).thenReturn(consulta);

        Consulta resultado = service.cancelar(consultaId);

        assertEquals(consulta, resultado);
        verify(consulta).setEstado(EstadoDaConsulta.CANCELADA);
        verify(listaDeEsperaService).promoverPrimeiroDaFila(
            medico,
            dataHora.toLocalDate()
        );
    }

    @Test
    void deveCriarContaComValorDaConsulta() {
        Medico medico = mock(Medico.class);
        Paciente paciente = mock(Paciente.class);
        LocalDateTime dataHora = LocalDate.now().plusDays(1).atTime(10, 30);
        BigDecimal valor = new BigDecimal("123.45");

        Consulta consulta = new Consulta(medico, paciente, dataHora, valor);
        consulta.setId(7L);

        when(medicoService.buscarPorId(1L)).thenReturn(medico);
        when(pacienteService.buscarPorId(2L)).thenReturn(paciente);
        when(agendamentoService.agendar(medico, paciente, dataHora)).thenReturn(
            consulta
        );
        when(contaRepository.save(any(Conta.class))).thenAnswer(inv ->
            inv.getArgument(0)
        );

        Consulta resultado = service.criar(
            new ConsultaRequest(1L, 2L, dataHora)
        );

        assertSame(consulta, resultado);
        ArgumentCaptor<Conta> captor = ArgumentCaptor.forClass(Conta.class);
        verify(contaRepository).save(captor.capture());

        Conta conta = captor.getValue();
        assertSame(consulta, conta.getConsulta());
        assertEquals(valor, conta.getValor());
        assertFalse(conta.isPaga());
    }
}
