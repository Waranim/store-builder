package xyz.waranim.authservice.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import xyz.waranim.authservice.dto.*;
import xyz.waranim.authservice.service.*;
import xyz.waranim.common.jwt.JwtUtils;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication API", description = "Управление аутентификацией и токенами")
@RequiredArgsConstructor
public class AuthController {
    private final OtpService otpService;
    private final AuthService authService;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtils jwtUtils;

    @Operation(
            summary = "Запрос OTP кода",
            description = "Отправляет одноразовый код подтверждения на указанный email"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Код успешно отправлен",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> sendOtp(
            @Parameter(
                    description = "Данные для запроса OTP",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequest.class))
            )
            @Valid @RequestBody LoginRequest request
    ) {
        userService.login(request.email(), true);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Подтверждение OTP кода",
            description = "Подтверждает одноразовый код и выдает токены доступа"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная аутентификация",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный OTP код",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PostMapping("/confirm")
    @Transactional
    public ResponseEntity<LoginResponse> confirmOtp(
            @Parameter(
                    description = "Данные подтверждения OTP",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ConfirmRequest.class))
            )
            @Valid @RequestBody ConfirmRequest request,
            HttpServletResponse response
    ) {
        if (!otpService.validateOtp(request.email(), request.otp())) {
            return ResponseEntity.badRequest().build();
        }

        userService.confirmEmail(request.email(), request.otp());

        AuthenticateResponse authenticateResponse = authService.authenticate(request.email());

        Cookie refreshCookie = new Cookie("refreshToken", authenticateResponse.refreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(new LoginResponse(authenticateResponse.accessToken()));
    }

    @Operation(
            summary = "Обновление токенов",
            description = "Обновляет пару access/refresh токенов по валидному refresh токену"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Токены успешно обновлены",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Невалидный или отсутствующий refresh токен",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<LoginResponse> refreshTokens(
            @Parameter(
                    description = "Refresh токен из cookies",
                    required = true,
                    hidden = true
            )
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return refreshTokenService.validateRefreshToken(refreshToken)
                .map(email -> {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    refreshTokenService.revokeRefreshToken(refreshToken);

                    String newAccessToken = jwtUtils.generateAccessToken(authentication);
                    String newRefreshToken = refreshTokenService.createRefreshToken(email);

                    Cookie cookie = new Cookie("refreshToken", newRefreshToken);
                    cookie.setHttpOnly(true);
                    cookie.setSecure(true);
                    cookie.setPath("/");
                    response.addCookie(cookie);

                    return ResponseEntity.ok(new LoginResponse(newAccessToken));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Operation(
            summary = "Регистрирует покупателя",
            description = "Отправляет одноразовый код подтверждения на указанный email и создаёт запись в сервисе заказов без поля fullname"
    )
    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(
            @Parameter(
                    description = "Данные для запроса OTP",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequest.class))
            )
            @Valid @RequestBody LoginRequest request
    ) {
        userService.login(request.email(), false);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @Hidden
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));
    }
}
