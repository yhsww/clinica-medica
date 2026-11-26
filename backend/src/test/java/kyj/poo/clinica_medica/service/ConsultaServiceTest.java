package kyj.poo.clinica_medica.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.EstadoDaConsulta;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.repository.ConsultaRepository;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

class ConsultaServiceTest {

    private final ConsultaRepository consultaRepository = mock(ConsultaRepository.class);
    private final ListaDeEsperaService listaDeEsperaService = mock(ListaDeEsperaService.class);
    private final ConsultaService service =
            new ConsultaService(consultaRepository, listaDeEsperaService);

    @Test
    void deveCancelarConsultaEPromoverPrimeiroDaFila() {
        Long consultaId = 1L;
        Medico medico = mock(Medico.class);
        Consulta consulta = mock(Consulta.class);
        LocalDateTime dataHora = LocalDate.now().plusDays(1).atTime(10, 30);

        when(consulta.getEstado()).thenReturn(EstadoDaConsulta.AGENDADA);
        when(consulta.getMedico()).thenReturn(medico);
        when(consulta.getDataHora()).thenReturn(dataHora);
        when(consultaRepository.findById(consultaId)).thenReturn(Optional.of(consulta));
        when(consultaRepository.save(consulta)).thenReturn(consulta);

        Consulta resultado = service.cancelar(consultaId);

        assertEquals(consulta, resultado);
        verify(consulta).setEstado(EstadoDaConsulta.CANCELADA);
        verify(listaDeEsperaService).promoverPrimeiroDaFila(medico, dataHora.toLocalDate());
    }
}
