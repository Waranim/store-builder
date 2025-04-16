package xyz.waranim.common.user;

import java.util.Set;

public record UserDto(Long id, String email, Set<String> roles) {
    public static UserDto fromEntity(UserEntity user) {
        return new UserDto(user.getId(), user.getEmail(), user.getRoles());
    }
}
