package xyz.waranim.authservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import xyz.waranim.authservice.entity.RefreshTokenEntity;
import xyz.waranim.authservice.repository.RefreshTokenRepository;
import xyz.waranim.common.jwt.JwtUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepo;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private final String testEmail = "user@example.com";
    private final String testToken = "test.refresh.token";
    private final Long testExpiration = 3600L;

    @Test
    void createRefreshToken_ShouldCreateNewTokenWithCorrectFields() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationSec", testExpiration);
        when(jwtUtils.generateRefreshToken(testEmail)).thenReturn(testToken);

        String result = refreshTokenService.createRefreshToken(testEmail);

        verify(refreshTokenRepo).deleteByEmail(testEmail);
        verify(jwtUtils).generateRefreshToken(testEmail);

        ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(refreshTokenRepo).save(captor.capture());

        RefreshTokenEntity savedToken = captor.getValue();
        assertEquals(testToken, savedToken.getToken());
        assertEquals(testEmail, savedToken.getEmail());
        assertEquals(testExpiration, savedToken.getExpiration());

        assertEquals(testToken, result);
    }

    @Test
    void validateRefreshToken_ShouldReturnEmailWhenTokenExists() {
        RefreshTokenEntity entity = new RefreshTokenEntity(testToken, testEmail, testExpiration);
        when(refreshTokenRepo.findById(testToken)).thenReturn(Optional.of(entity));

        Optional<String> result = refreshTokenService.validateRefreshToken(testToken);

        assertTrue(result.isPresent());
        assertEquals(testEmail, result.get());
    }

    @Test
    void validateRefreshToken_ShouldReturnEmptyForNonExistingToken() {
        when(refreshTokenRepo.findById(testToken)).thenReturn(Optional.empty());

        Optional<String> result = refreshTokenService.validateRefreshToken(testToken);

        assertTrue(result.isEmpty());
    }

    @Test
    void revokeRefreshToken_ShouldDeleteTokenFromRepository() {
        refreshTokenService.revokeRefreshToken(testToken);

        verify(refreshTokenRepo).deleteById(testToken);
    }

    @Test
    void createRefreshToken_ShouldUseConfiguredExpiration() {
        Long expectedExpiration = 7200L;
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationSec", expectedExpiration);
        when(jwtUtils.generateRefreshToken(testEmail)).thenReturn(testToken);

        refreshTokenService.createRefreshToken(testEmail);

        ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(refreshTokenRepo).save(captor.capture());

        assertEquals(expectedExpiration, captor.getValue().getExpiration());
    }

    @Test
    void createRefreshToken_ShouldOverwriteExistingTokens() {
        when(jwtUtils.generateRefreshToken(testEmail)).thenReturn(testToken);

        refreshTokenService.createRefreshToken(testEmail);

        verify(refreshTokenRepo).deleteByEmail(testEmail);
        verify(refreshTokenRepo, times(1)).save(any());
    }
}