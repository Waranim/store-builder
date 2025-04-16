package xyz.waranim.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.waranim.authservice.exception.InvalidOtpException;
import xyz.waranim.authservice.exception.UserNotFoundException;
import xyz.waranim.authservice.repository.UserRepository;
import xyz.waranim.common.user.UserEntity;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final OtpService otpService;

    public void login(String email) {
        if (!userRepository.existsByEmail(email)) {
            UserEntity user = new UserEntity();
            user.setEmail(email);
            user.getRoles().add("USER");
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
