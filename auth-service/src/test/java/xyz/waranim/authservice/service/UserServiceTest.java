package xyz.waranim.authservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.waranim.authservice.exception.InvalidOtpException;
import xyz.waranim.authservice.exception.UserNotFoundException;
import xyz.waranim.authservice.repository.UserRepository;
import xyz.waranim.common.user.UserEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private UserService userService;

    private final String validEmail = "user@example.com";
    private final String validOtp = "123456";
    private final String invalidOtp = "000000";

    @Test
    void login_Seller_WithNewUser_ShouldCreateUserAndGenerateOtp() {
        when(userRepository.existsByEmail(validEmail)).thenReturn(false);

        userService.login(validEmail, true);

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals(validEmail) && !user.isEmailVerified()
        ));
        verify(otpService).generateOtp(validEmail);
    }

    @Test
    void login_Seller_WithExistingUser_ShouldOnlyGenerateOtp() {
        when(userRepository.existsByEmail(validEmail)).thenReturn(true);

        userService.login(validEmail, true);

        verify(userRepository, never()).save(any());
        verify(otpService).generateOtp(validEmail);
    }

    @Test
    void confirmEmail_WithInvalidOtp_ShouldThrowInvalidOtpException() {
        when(otpService.validateOtp(validEmail, invalidOtp)).thenReturn(false);

        assertThrows(InvalidOtpException.class, () ->
                userService.confirmEmail(validEmail, invalidOtp)
        );
        verifyNoInteractions(userRepository);
    }

    @Test
    void confirmEmail_WithValidOtpAndExistingUser_ShouldVerifyEmail() {
        UserEntity user = new UserEntity();
        user.setEmail(validEmail);

        when(otpService.validateOtp(validEmail, validOtp)).thenReturn(true);
        when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(user));

        userService.confirmEmail(validEmail, validOtp);

        assertTrue(user.isEmailVerified());
        verify(userRepository).save(user);
    }

    @Test
    void confirmEmail_WithValidOtpButMissingUser_ShouldThrowUserNotFoundException() {
        when(otpService.validateOtp(validEmail, validOtp)).thenReturn(true);
        when(userRepository.findByEmail(validEmail)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                userService.confirmEmail(validEmail, validOtp)
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    void confirmEmail_ShouldCallRepositoryWithCorrectEmail() {
        UserEntity user = new UserEntity();
        user.setEmail(validEmail);

        when(otpService.validateOtp(validEmail, validOtp)).thenReturn(true);
        when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(user));

        userService.confirmEmail(validEmail, validOtp);

        verify(userRepository).findByEmail(validEmail);
        verify(userRepository).save(user);
    }
}