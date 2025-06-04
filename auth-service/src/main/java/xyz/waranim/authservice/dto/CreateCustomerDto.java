package xyz.waranim.authservice.dto;

public record CreateCustomerDto(
        String email,
        String fullName
) {
}
