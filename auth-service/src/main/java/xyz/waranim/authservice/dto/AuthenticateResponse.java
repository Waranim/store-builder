package xyz.waranim.authservice.dto;

public record AuthenticateResponse(String accessToken, String refreshToken) {
}
