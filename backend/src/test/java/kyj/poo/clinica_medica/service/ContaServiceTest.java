package kyj.poo.clinica_medica.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import kyj.poo.clinica_medica.exception.OperacaoInvalidaException;
import kyj.poo.clinica_medica.model.Conta;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.repository.ContaRepository;
import org.junit.jupiter.api.Test;

class ContaServiceTest {

    private final ContaRepository contaRepository = mock(ContaRepository.class);
    private final ContaService servico = new ContaService(contaRepository);

    @Test
    void devePagarConta() {
        Conta conta = new Conta(null, BigDecimal.TEN);
        conta.setId(10L);

        when(contaRepository.findById(10L)).thenReturn(Optional.of(conta));
        when(contaRepository.save(any(Conta.class))).thenAnswer(inv ->
            inv.getArgument(0)
        );

        Conta paga = servico.pagar(10L);

        assertTrue(paga.isPaga());
    }

    @Test
    void naoDevePagarContaJaPaga() {
        Conta conta = new Conta(null, new BigDecimal("100.00"));
        conta.setId(5L);
        conta.setPaga(true);

        when(contaRepository.findById(5L)).thenReturn(Optional.of(conta));

        assertThrows(OperacaoInvalidaException.class, () ->
            servico.pagar(5L)
        );
        verify(contaRepository, never()).save(any());
    }

    @Test
    void deveListarPorPacienteEPorMedico() {
        Paciente paciente = mock(Paciente.class);
        Medico medico = mock(Medico.class);
        Conta conta = new Conta(null, BigDecimal.TEN);

        when(contaRepository.findByConsulta_Paciente(paciente)).thenReturn(
            List.of(conta)
        );
        when(contaRepository.findByConsulta_Medico(medico)).thenReturn(
            List.of(conta)
        );

        assertEquals(List.of(conta), servico.listarPorPaciente(paciente));
        assertEquals(List.of(conta), servico.listarPorMedico(medico));
        assertThrows(IllegalArgumentException.class, () ->
            servico.listarPorPaciente(null)
        );
        assertThrows(IllegalArgumentException.class, () ->
            servico.listarPorMedico(null)
        );
    }

    @Test
    void deveListarPendentesEPagas() {
        Paciente paciente = mock(Paciente.class);
        Conta pendente = new Conta(null, BigDecimal.TEN);
        Conta paga = new Conta(null, BigDecimal.TEN);
        paga.setPaga(true);

        when(contaRepository.findByPagaFalse()).thenReturn(List.of(pendente));
        when(contaRepository.findByPagaTrue()).thenReturn(List.of(paga));
        when(contaRepository.findByConsulta_Paciente(paciente)).thenReturn(
            List.of(pendente, paga)
        );

        assertEquals(List.of(pendente), servico.listarPendentes());
        assertEquals(List.of(paga), servico.listarPagas());
        assertEquals(List.of(pendente), servico.listarPendentes(paciente));
        assertEquals(List.of(paga), servico.listarPagas(paciente));
    }
}
