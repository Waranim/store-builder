package xyz.waranim.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {
    @InjectMocks
    private JwtUtils jwtUtils;

    private final User user = new User(
            "test@example.com",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

    @BeforeEach
    void setUp() {
        String secret = "testSecretKeyWithAtLeast32CharactersLong!";
        long refreshExpiration = 86400;
        long accessExpiration = 900;
        ReflectionTestUtils.setField(jwtUtils, "secret", secret);
        ReflectionTestUtils.setField(jwtUtils, "refreshExpirationMs", refreshExpiration * 1000);
        ReflectionTestUtils.setField(jwtUtils, "accessExpirationMs", accessExpiration * 1000);

        jwtUtils.init();
    }

    @Test
    void generateAccessToken_ShouldContainCorrectClaims() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());

        String token = jwtUtils.generateAccessToken(authentication);
        Claims claims = jwtUtils.parseToken(token);

        assertEquals("test@example.com", claims.getSubject());
        assertEquals(List.of("ROLE_USER"), claims.get("roles"));
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void generateRefreshToken_ShouldHaveCorrectStructure() {
        String token = jwtUtils.generateRefreshToken("test@example.com");
        Claims claims = jwtUtils.parseToken(token);

        assertEquals("test@example.com", claims.getSubject());
        assertEquals("REFRESH", claims.get("type"));
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void validateToken_ShouldReturnTrueForValidToken() {
        String token = jwtUtils.generateAccessToken(
                new UsernamePasswordAuthenticationToken(user, null));

        assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    void validateToken_ShouldReturnFalseForExpiredToken() {
        ReflectionTestUtils.setField(jwtUtils, "accessExpirationMs", -3600L);

        Authentication auth = new UsernamePasswordAuthenticationToken(user, null);

        String token = jwtUtils.generateAccessToken(auth);

        assertFalse(jwtUtils.validateToken(token));
    }

    @Test
    void validateToken_ShouldReturnFalseForInvalidSignature() {
        String validToken = jwtUtils.generateAccessToken(new UsernamePasswordAuthenticationToken(user, null));
        String invalidToken = validToken + "tampered";

        assertFalse(jwtUtils.validateToken(invalidToken));
    }

    @Test
    void extractEmail_ShouldReturnCorrectEmail() {
        String token = jwtUtils.generateRefreshToken("test@example.com");

        assertEquals("test@example.com", jwtUtils.extractEmail(token));
    }

    @Test
    void parseToken_ShouldThrowForMalformedToken() {
        assertThrows(MalformedJwtException.class,
                () -> jwtUtils.parseToken("invalid.token"));
    }

    @Test
    void init_ShouldGenerateValidKeyFromSecret() {
        assertNotNull(ReflectionTestUtils.getField(jwtUtils, "key"));
    }
}