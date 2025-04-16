package xyz.waranim.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import xyz.waranim.authservice.dto.AuthenticateResponse;
import xyz.waranim.common.jwt.JwtUtils;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthenticateResponse authenticate(String email) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        String accessToken = jwtUtils.generateAccessToken(authentication);
        String refreshToken = refreshTokenService.createRefreshToken(email);

        return new AuthenticateResponse(accessToken, refreshToken);
    }
}
