package xyz.waranim.authservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import xyz.waranim.authservice.dto.AuthenticateResponse;
import xyz.waranim.common.jwt.JwtUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @InjectMocks
    private AuthService authService;

    private final String email = "user@example.com";
    private final String accessToken = "access-token";
    private final String refreshToken = "refresh-token";
    private final UserDetails userDetails = User.builder()
            .username(email)
            .password("")
            .authorities(Collections.singletonList(() -> "ROLE_USER"))
            .build();

    @Test
    void authenticate_ShouldReturnValidTokens_WhenUserExists() {
        when(customUserDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtUtils.generateAccessToken(any(Authentication.class))).thenReturn(accessToken);
        when(refreshTokenService.createRefreshToken(email)).thenReturn(refreshToken);

        AuthenticateResponse response = authService.authenticate(email);

        assertNotNull(response);
        assertEquals(accessToken, response.accessToken());
        assertEquals(refreshToken, response.refreshToken());

        verify(customUserDetailsService).loadUserByUsername(email);

        ArgumentCaptor<Authentication> authCaptor = ArgumentCaptor.forClass(Authentication.class);
        verify(jwtUtils).generateAccessToken(authCaptor.capture());

        Authentication capturedAuth = authCaptor.getValue();
        assertEquals(userDetails, capturedAuth.getPrincipal());
        assertTrue(capturedAuth.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList()
                .containsAll(
                        userDetails.getAuthorities()
                                .stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList()
                )
        );

        verify(refreshTokenService).createRefreshToken(email);
    }

    @Test
    void authenticate_ShouldThrowException_WhenUserNotFound() {
        when(customUserDetailsService.loadUserByUsername(email))
                .thenThrow(new UsernameNotFoundException("User not found"));

        assertThrows(UsernameNotFoundException.class, () ->
                authService.authenticate(email)
        );

        verifyNoInteractions(jwtUtils);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void authenticate_ShouldIncludeCorrectAuthorities_InAccessToken() {
        UserDetails adminDetails = User.builder()
                .username(email)
                .password("")
                .authorities(Collections.singletonList(() -> "ROLE_ADMIN"))
                .build();

        when(customUserDetailsService.loadUserByUsername(email)).thenReturn(adminDetails);
        when(jwtUtils.generateAccessToken(any())).thenReturn(accessToken);
        when(refreshTokenService.createRefreshToken(email)).thenReturn(refreshToken);

        AuthenticateResponse response = authService.authenticate(email);

        verify(jwtUtils).generateAccessToken(argThat(auth ->
                auth.getAuthorities().stream()
                        .anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"))
        ));
    }

    @Test
    void authenticate_ShouldCreateNewRefreshToken_EachTime() {
        String newRefreshToken = "new-refresh-token";

        when(customUserDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtUtils.generateAccessToken(any())).thenReturn(accessToken);
        when(refreshTokenService.createRefreshToken(email))
                .thenReturn(refreshToken)
                .thenReturn(newRefreshToken);

        AuthenticateResponse firstResponse = authService.authenticate(email);

        AuthenticateResponse secondResponse = authService.authenticate(email);

        assertEquals(refreshToken, firstResponse.refreshToken());
        assertEquals(newRefreshToken, secondResponse.refreshToken());
        verify(refreshTokenService, times(2)).createRefreshToken(email);
    }
}