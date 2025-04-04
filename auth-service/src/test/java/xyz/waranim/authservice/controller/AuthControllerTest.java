package xyz.waranim.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import xyz.waranim.authservice.config.SecurityConfig;
import xyz.waranim.authservice.dto.AuthenticateResponse;
import xyz.waranim.authservice.dto.ConfirmRequest;
import xyz.waranim.authservice.dto.LoginRequest;
import xyz.waranim.authservice.service.*;
import xyz.waranim.common.jwt.JwtUtils;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OtpService otpService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtUtils jwtUtils;

    private final String testEmail = "test@example.com";
    private final String testOtp = "123456";
    private final String testToken = "test.token";

    @Test
    void sendOtp_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(testEmail))))
                .andExpect(status().isOk());

        verify(userService).login(testEmail);
    }

    @Test
    void confirmOtp_ShouldReturnTokensAndSetCookie() throws Exception {
        when(otpService.validateOtp(testEmail, testOtp)).thenReturn(true);
        when(authService.authenticate(testEmail))
                .thenReturn(new AuthenticateResponse("access", "refresh"));

        MockHttpServletResponse response = mockMvc.perform(post("/api/v1/auth/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ConfirmRequest(testEmail, testOtp))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andReturn().getResponse();

        Cookie cookie = response.getCookie("refreshToken");
        assertNotNull(cookie);
        assertEquals("refresh", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getSecure());
        assertEquals("/", cookie.getPath());

        verify(userService).confirmEmail(testEmail, testOtp);
    }

    @Test
    void confirmOtp_ShouldReturnBadRequestForInvalidOtp() throws Exception {
        when(otpService.validateOtp(testEmail, testOtp)).thenReturn(false);

        mockMvc.perform(post("/api/v1/auth/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ConfirmRequest(testEmail, testOtp))))
                .andExpect(status().isBadRequest());

        verify(userService, never()).confirmEmail(any(), any());
    }

    @Test
    void refreshTokens_ShouldReturnNewTokens() throws Exception {
        UserDetails userDetails = new User(testEmail, "", Collections.emptyList());
        when(customUserDetailsService.loadUserByUsername(testEmail)).thenReturn(userDetails);
        when(refreshTokenService.validateRefreshToken(testToken)).thenReturn(Optional.of(testEmail));
        when(jwtUtils.generateAccessToken(any())).thenReturn("newAccess");
        when(refreshTokenService.createRefreshToken(testEmail)).thenReturn("newRefresh");

        MockHttpServletResponse response = mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refreshToken", testToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccess"))
                .andReturn().getResponse();

        Cookie cookie = response.getCookie("refreshToken");
        assert cookie != null;
        assertEquals("newRefresh", cookie.getValue());

        verify(refreshTokenService).revokeRefreshToken(testToken);
    }

    @Test
    void refreshTokens_ShouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshTokens_ShouldReturnUnauthorizedForInvalidToken() throws Exception {
        when(refreshTokenService.validateRefreshToken(testToken)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refreshToken", testToken)))
                .andExpect(status().isUnauthorized());
    }
}