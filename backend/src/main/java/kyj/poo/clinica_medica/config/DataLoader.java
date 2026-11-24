package kyj.poo.clinica_medica.config;

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
import kyj.poo.clinica_medica.repository.CredencialRepository;
import kyj.poo.clinica_medica.repository.EspecialidadeRepository;
import kyj.poo.clinica_medica.repository.MedicoRepository;
import kyj.poo.clinica_medica.repository.PacienteRepository;
import kyj.poo.clinica_medica.repository.PlanoRepository;
import kyj.poo.clinica_medica.service.CredencialService;

import java.math.BigDecimal;
import java.util.List;

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
            CredencialService credencialService) {

        return args -> {
            criarAdmin(pacienteRepository, credencialRepository, credencialService);

            for (PlanoNome plano : PlanoNome.values()) {
                resolverPlano(planoRepository, plano.getNome());
            }

            Especialidade cardiologia =
                    resolverEspecialidade(especialidadeRepository, new Cardiologia());
            Especialidade dermatologia =
                    resolverEspecialidade(especialidadeRepository, new Dermatologia());
            resolverEspecialidade(especialidadeRepository, new ClinicoGeral());
            resolverEspecialidade(especialidadeRepository, new Pediatria());

            criarMedico(medicoRepository, credencialRepository, credencialService,
                    "Dr. Carlos Lima", "medico@clinica.com", new BigDecimal("250.00"), cardiologia);
            criarMedico(medicoRepository, credencialRepository, credencialService,
                    "Dra. Ana Paula", "ana@clinica.com", new BigDecimal("300.00"), dermatologia);

            criarPaciente(pacienteRepository, credencialRepository, credencialService,
                    "Maria Silva", "paciente@clinica.com", 34, Papel.PACIENTE);
            criarPaciente(pacienteRepository, credencialRepository, credencialService,
                    "João Pereira", "joao@clinica.com", 28, Papel.PACIENTE);
        };
    }

    private Especialidade resolverEspecialidade(
            EspecialidadeRepository especialidadeRepository, Especialidade especialidade) {

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
            CredencialService credencialService) {

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
            Especialidade especialidade) {

        List<Medico> existentes = medicoRepository.findByEmail(email);

        if (existentes.isEmpty()) {
            Medico medico = new Medico();
            medico.setNome(nome);
            medico.setEmail(email);
            medico.setEspecialidade(especialidade);
            medico.setValorConsultaParticular(valor);
            medicoRepository.save(medico);

            if (!credencialRepository.existsByUsuario_Email(email)) {
                credencialService.criar(medico, "senha123", Papel.MEDICO);
            }
            return;
        }

        for (Medico medico : existentes) {
            if (medico.getEspecialidade() == null) {
                medico.setEspecialidade(especialidade);
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
            Papel papel) {

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
