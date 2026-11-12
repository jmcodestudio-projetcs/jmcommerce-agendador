package br.com.jmcodestudio.agendador.auth;

import br.com.jmcodestudio.agendador.fotografo.FotografoRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Ensina o Spring Security a buscar o fotografo pelo e-mail na hora do login. */
@Service
public class FotografoUserDetailsService implements UserDetailsService {

    private final FotografoRepository repository;

    public FotografoUserDetailsService(FotografoRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmail(email)
                .map(f -> User.withUsername(f.getEmail())
                        .password(f.getSenhaHash())
                        .authorities("FOTOGRAFO")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Fotografo nao encontrado."));
    }
}
