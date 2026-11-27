package kyj.poo.clinica_medica.security;

import java.util.Collection;
import java.util.List;
import kyj.poo.clinica_medica.model.Credencial;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CredencialUserDetails implements UserDetails {

    private final Credencial credencial;

    public CredencialUserDetails(Credencial credencial) {
        this.credencial = credencial;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
            new SimpleGrantedAuthority("ROLE_" + credencial.getPapel().name())
        );
    }

    @Override
    public String getPassword() {
        return credencial.getSenha();
    }

    @Override
    public String getUsername() {
        return credencial.getUsuario().getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return credencial.isAtivo();
    }

    public Credencial getCredencial() {
        return credencial;
    }
}
