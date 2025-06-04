package xyz.waranim.notificationservice.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email
) {
}
