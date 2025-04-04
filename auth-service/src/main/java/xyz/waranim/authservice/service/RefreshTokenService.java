package xyz.waranim.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.waranim.authservice.entity.RefreshTokenEntity;
import xyz.waranim.authservice.repository.RefreshTokenRepository;
import xyz.waranim.common.jwt.JwtUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepo;
    private final JwtUtils jwtUtils;

    @Value("${jwt.refresh.expiration}")
    private Long refreshExpirationSec;

    public String createRefreshToken(String email) {
        refreshTokenRepo.deleteByEmail(email);

        String token = jwtUtils.generateRefreshToken(email);

        RefreshTokenEntity refreshToken = new RefreshTokenEntity(token, email, refreshExpirationSec);
        refreshTokenRepo.save(refreshToken);

        return token;
    }

    public Optional<String> validateRefreshToken(String token) {
        return refreshTokenRepo.findById(token)
                .map(RefreshTokenEntity::getEmail);
    }

    public void revokeRefreshToken(String token) {
        refreshTokenRepo.deleteById(token);
    }
}
