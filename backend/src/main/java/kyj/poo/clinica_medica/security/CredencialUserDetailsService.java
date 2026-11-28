package kyj.poo.clinica_medica.security;

import kyj.poo.clinica_medica.model.Credencial;
import kyj.poo.clinica_medica.repository.CredencialRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CredencialUserDetailsService implements UserDetailsService {

    private final CredencialRepository repository;

    public CredencialUserDetailsService(CredencialRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(
        String email
    ) throws UsernameNotFoundException {
        Credencial credencial = repository
            .findByUsuario_Email(email)
            .orElseThrow(() ->
                new UsernameNotFoundException(
                    "Credencial não encontrada para o e-mail: " + email
                )
            );

        return new CredencialUserDetails(credencial);
    }
}
