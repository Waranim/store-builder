package xyz.waranim.authservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import xyz.waranim.authservice.repository.UserRepository;
import xyz.waranim.common.user.UserEntity;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private final String email = "user@example.com";
    private final String blockedEmail = "blocked@example.com";
    private final String unverifiedEmail = "unverified@example.com";

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExistsAndVerified() {
        UserEntity user = createUser(email, true, true, Set.of("USER", "ADMIN"));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(email)
        );
        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenEmailNotVerified() {
        UserEntity user = createUser(unverifiedEmail, false, true, Set.of("USER"));
        when(userRepository.findByEmail(unverifiedEmail)).thenReturn(Optional.of(user));

        DisabledException exception = assertThrows(
                DisabledException.class,
                () -> userDetailsService.loadUserByUsername(unverifiedEmail)
        );
        assertEquals("Почта не подтверждена", exception.getMessage());
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserDisabled() {
        UserEntity user = createUser(blockedEmail, true, false, Set.of("USER"));
        when(userRepository.findByEmail(blockedEmail)).thenReturn(Optional.of(user));

        DisabledException exception = assertThrows(
                DisabledException.class,
                () -> userDetailsService.loadUserByUsername(blockedEmail)
        );
        assertEquals("Аккаунт заблокирован", exception.getMessage());
    }

    @Test
    void loadUserByUsername_ShouldReturnEmptyAuthorities_WhenNoRoles() {
        UserEntity user = createUser(email, true, true, Set.of());
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertTrue(userDetails.getAuthorities().isEmpty());
    }

    private UserEntity createUser(String email, boolean verified, boolean enabled, Set<String> roles) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setEmailVerified(verified);
        user.setEnabled(enabled);
        user.setRoles(roles);
        return user;
    }
}