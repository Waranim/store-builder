package xyz.waranim.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.waranim.authservice.dto.CreateCustomerDto;
import xyz.waranim.authservice.dto.UserResponse;
import xyz.waranim.authservice.exception.InvalidOtpException;
import xyz.waranim.authservice.exception.UserNotFoundException;
import xyz.waranim.authservice.feign.OrderClient;
import xyz.waranim.authservice.repository.UserRepository;
import xyz.waranim.common.user.UserEntity;
import xyz.waranim.common.user.UserRole;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final OrderClient orderClient;

    public void login(String email, boolean isSeller) {
        if (!userRepository.existsByEmail(email)) {
            UserEntity user = new UserEntity();
            user.setEmail(email);
            if (isSeller) {
                user.setRole(UserRole.SELLER);
            } else {
                user.setRole(UserRole.CUSTOMER);
            }
            userRepository.save(user);

            if (!isSeller) {
                orderClient.createCustomer(user.getId(), new CreateCustomerDto(email, ""));
            }
        }

        otpService.generateOtp(email);
    }

    public void confirmEmail(String email, String otp) {
        if (!otpService.validateOtp(email, otp)) {
            throw new InvalidOtpException("Неверный OTP");
        }

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        user.setEmailVerified(true);
        userRepository.save(user);
    }

    public UserResponse getById(UUID id) {
        UserEntity user = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("Пользователь не найден: " + id.toString()));

        return new UserResponse(user.getId(), user.getEmail());
    }
}
