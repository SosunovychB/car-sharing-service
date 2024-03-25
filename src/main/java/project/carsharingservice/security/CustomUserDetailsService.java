package project.carsharingservice.security;

import lombok.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.*;
import project.carsharingservice.repository.*;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Can't find user with email " + email));
    }
}
