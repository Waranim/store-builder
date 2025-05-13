package xyz.waranim.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.waranim.authservice.exception.InvalidOtpException;
import xyz.waranim.authservice.exception.UserNotFoundException;
import xyz.waranim.authservice.repository.UserRepository;
import xyz.waranim.common.user.UserEntity;
import xyz.waranim.common.user.UserRole;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final OtpService otpService;

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
}
