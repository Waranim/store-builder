package xyz.waranim.common.user;

import java.util.UUID;

public record UserDto(UUID id, String email, UserRole role) {
    public static UserDto fromEntity(UserEntity user) {
        return new UserDto(user.getId(), user.getEmail(), user.getRole());
    }
}
