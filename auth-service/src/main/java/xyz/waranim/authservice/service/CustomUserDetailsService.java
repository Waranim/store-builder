package xyz.waranim.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import xyz.waranim.authservice.repository.UserRepository;
import xyz.waranim.common.user.CustomUserDetails;
import xyz.waranim.common.user.UserEntity;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        if (!user.isEmailVerified()) {
            throw new DisabledException("Почта не подтверждена");
        }

        if (!user.isEnabled()) {
            throw new DisabledException("Аккаунт заблокирован");
        }

        List<SimpleGrantedAuthority> authorities;
        if (user.getRole() != null) {
            authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        } else {
            authorities = List.of();
        }

        return new CustomUserDetails(
                String.valueOf(user.getId()),
                user.getEmail(),
                "",
                authorities
        );
    }
}
