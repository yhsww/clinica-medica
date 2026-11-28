package kyj.poo.clinica_medica.config;

import java.math.BigDecimal;
import java.util.List;
import kyj.poo.clinica_medica.model.Consulta;
import kyj.poo.clinica_medica.model.Conta;
import kyj.poo.clinica_medica.model.EstadoDaConsulta;
import kyj.poo.clinica_medica.model.Medico;
import kyj.poo.clinica_medica.model.Paciente;
import kyj.poo.clinica_medica.model.Papel;
import kyj.poo.clinica_medica.model.Plano;
import kyj.poo.clinica_medica.model.PlanoNome;
import kyj.poo.clinica_medica.model.especialidade.Cardiologia;
import kyj.poo.clinica_medica.model.especialidade.ClinicoGeral;
import kyj.poo.clinica_medica.model.especialidade.Dermatologia;
import kyj.poo.clinica_medica.model.especialidade.Especialidade;
import kyj.poo.clinica_medica.model.especialidade.Pediatria;
import kyj.poo.clinica_medica.repository.ConsultaRepository;
import kyj.poo.clinica_medica.repository.ContaRepository;
import kyj.poo.clinica_medica.repository.CredencialRepository;
import kyj.poo.clinica_medica.repository.EspecialidadeRepository;
import kyj.poo.clinica_medica.repository.MedicoRepository;
import kyj.poo.clinica_medica.repository.PacienteRepository;
import kyj.poo.clinica_medica.repository.PlanoRepository;
import kyj.poo.clinica_medica.service.CredencialService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class DataLoader {

    @Bean
    @Order(1)
    CommandLineRunner carregarMocks(
        PacienteRepository pacienteRepository,
        MedicoRepository medicoRepository,
        CredencialRepository credencialRepository,
        EspecialidadeRepository especialidadeRepository,
        PlanoRepository planoRepository,
        CredencialService credencialService
    ) {
        return args -> {
            criarAdmin(
                pacienteRepository,
                credencialRepository,
                credencialService
            );

            for (PlanoNome plano : PlanoNome.values()) {
                resolverPlano(planoRepository, plano.getNome());
            }

            Plano sus = resolverPlano(planoRepository, PlanoNome.SUS.getNome());

            Especialidade cardiologia = resolverEspecialidade(
                especialidadeRepository,
                new Cardiologia()
            );
            Especialidade dermatologia = resolverEspecialidade(
                especialidadeRepository,
                new Dermatologia()
            );
            resolverEspecialidade(especialidadeRepository, new ClinicoGeral());
            resolverEspecialidade(especialidadeRepository, new Pediatria());

            // Cardiologista atende SUS; dermatologista só particular
            criarMedico(
                medicoRepository,
                credencialRepository,
                credencialService,
                "Dr. Carlos Lima",
                "medico@clinica.com",
                new BigDecimal("250.00"),
                cardiologia,
                List.of(sus)
            );
            criarMedico(
                medicoRepository,
                credencialRepository,
                credencialService,
                "Dra. Ana Paula",
                "ana@clinica.com",
                new BigDecimal("300.00"),
                dermatologia,
                List.of()
            );

            criarPaciente(
                pacienteRepository,
                credencialRepository,
                credencialService,
                "Maria Silva",
                "paciente@clinica.com",
                34,
                Papel.PACIENTE
            );
            criarPaciente(
                pacienteRepository,
                credencialRepository,
                credencialService,
                "João Pereira",
                "joao@clinica.com",
                28,
                Papel.PACIENTE
            );
        };
    }

    /**
     * Garante conta para consultas antigas que deveriam ter uma: não
     * canceladas e com valor a pagar.
     */
    @Bean
    @Order(2)
    CommandLineRunner criarContasFaltantes(
        ConsultaRepository consultaRepository,
        ContaRepository contaRepository
    ) {
        return args -> {
            for (Consulta consulta : consultaRepository.findAll()) {
                if (
                    consulta.getEstado() == EstadoDaConsulta.CANCELADA ||
                    consulta.getValorCobrado() == null ||
                    consulta.getValorCobrado().signum() <= 0 ||
                    contaRepository.findByConsulta(consulta).isPresent()
                ) {
                    continue;
                }

                Conta conta = new Conta(consulta, consulta.getValorCobrado());
                contaRepository.save(conta);
            }
        };
    }

    private Especialidade resolverEspecialidade(
        EspecialidadeRepository especialidadeRepository,
        Especialidade especialidade
    ) {
        return especialidadeRepository
            .findByNomeIgnoreCase(especialidade.getNome())
            .orElseGet(() -> especialidadeRepository.save(especialidade));
    }

    private Plano resolverPlano(PlanoRepository planoRepository, String nome) {
        return planoRepository
            .findByNomeIgnoreCase(nome)
            .orElseGet(() -> planoRepository.save(new Plano(nome)));
    }

    private void criarAdmin(
        PacienteRepository pacienteRepository,
        CredencialRepository credencialRepository,
        CredencialService credencialService
    ) {
        String email = "admin@clinica.com";

        if (credencialRepository.existsByUsuario_Email(email)) {
            return;
        }

        Paciente admin = new Paciente();
        admin.setNome("Admin");
        admin.setEmail(email);
        admin.setIdade(null);
        admin.setPlanoSaude(null);
        pacienteRepository.save(admin);

        credencialService.criar(admin, "admin", Papel.ADMIN);
    }

    private void criarMedico(
        MedicoRepository medicoRepository,
        CredencialRepository credencialRepository,
        CredencialService credencialService,
        String nome,
        String email,
        BigDecimal valor,
        Especialidade especialidade,
        List<Plano> planosAceitos
    ) {
        List<Medico> existentes = medicoRepository.findByEmail(email);

        if (existentes.isEmpty()) {
            Medico medico = new Medico();
            medico.setNome(nome);
            medico.setEmail(email);
            medico.setEspecialidade(especialidade);
            medico.setValorConsultaParticular(valor);
            planosAceitos.forEach(medico::adicionarPlanoAceito);
            medicoRepository.save(medico);

            if (!credencialRepository.existsByUsuario_Email(email)) {
                credencialService.criar(medico, "senha123", Papel.MEDICO);
            }
            return;
        }

        for (Medico medico : existentes) {
            boolean alterado = false;

            if (medico.getEspecialidade() == null) {
                medico.setEspecialidade(especialidade);
                alterado = true;
            }

            // Bancos criados antes desta versão: aplica os planos padrão
            if (medico.getPlanosAceitos().isEmpty() && !planosAceitos.isEmpty()) {
                planosAceitos.forEach(medico::adicionarPlanoAceito);
                alterado = true;
            }

            if (alterado) {
                medicoRepository.save(medico);
            }
        }
    }

    private void criarPaciente(
        PacienteRepository pacienteRepository,
        CredencialRepository credencialRepository,
        CredencialService credencialService,
        String nome,
        String email,
        Integer idade,
        Papel papel
    ) {
        if (credencialRepository.existsByUsuario_Email(email)) {
            return;
        }

        Paciente paciente = new Paciente();
        paciente.setNome(nome);
        paciente.setEmail(email);
        paciente.setIdade(idade);
        paciente.setPlanoSaude(null);
        pacienteRepository.save(paciente);

        credencialService.criar(paciente, "senha123", papel);
    }
}
