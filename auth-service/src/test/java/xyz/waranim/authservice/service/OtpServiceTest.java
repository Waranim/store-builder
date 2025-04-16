package xyz.waranim.authservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSender;
import org.springframework.test.util.ReflectionTestUtils;
import xyz.waranim.authservice.entity.OtpEntity;
import xyz.waranim.authservice.repository.OtpRepository;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private MailSender mailSender;

    @Mock
    private Random random;

    @InjectMocks
    private OtpService otpService;

    private final String email = "user@example.com";

    @Test
    void generateOtp_ShouldGenerateAndSaveValidCode() {
        ReflectionTestUtils.setField(otpService, "random", random);
        when(random.nextInt(999999)).thenReturn(123456);
        Long expiration = 300L;
        ReflectionTestUtils.setField(otpService, "expiration", expiration);

        String otp = otpService.generateOtp(email);

        assertEquals("123456", otp);

        ArgumentCaptor<OtpEntity> otpCaptor = ArgumentCaptor.forClass(OtpEntity.class);
        verify(otpRepository).save(otpCaptor.capture());

        OtpEntity savedOtp = otpCaptor.getValue();
        assertEquals(email, savedOtp.getEmail());
        assertEquals(otp, savedOtp.getCode());
        assertEquals(expiration, savedOtp.getExpiration());
    }

    @Test
    void validateOtp_ShouldReturnTrue_WhenCodeMatches() {
        String validOtp = "654321";
        OtpEntity otpEntity = new OtpEntity();
        otpEntity.setCode(validOtp);

        when(otpRepository.findById(email)).thenReturn(Optional.of(otpEntity));

        boolean isValid = otpService.validateOtp(email, validOtp);

        assertTrue(isValid);
    }

    @Test
    void validateOtp_ShouldReturnFalse_WhenCodeNotMatches() {
        String storedOtp = "654321";
        String inputOtp = "wrong123";
        OtpEntity otpEntity = new OtpEntity();
        otpEntity.setCode(storedOtp);

        when(otpRepository.findById(email)).thenReturn(Optional.of(otpEntity));

        boolean isValid = otpService.validateOtp(email, inputOtp);

        assertFalse(isValid);
    }

    @Test
    void validateOtp_ShouldReturnFalse_WhenNoOtpExists() {
        when(otpRepository.findById(email)).thenReturn(Optional.empty());

        boolean isValid = otpService.validateOtp(email, "anyCode");

        assertFalse(isValid);
    }

    @Test
    void generateOtp_ShouldOverwriteExistingOtp() {
        ReflectionTestUtils.setField(otpService, "random", random);
        when(random.nextInt(999999))
                .thenReturn(111111)
                .thenReturn(222222);

        otpService.generateOtp(email);

        String newOtp = otpService.generateOtp(email);

        verify(otpRepository, times(2)).save(any());
        assertEquals("222222", newOtp);
    }

    @Test
    void generatedOtp_ShouldAlwaysBe6Digits() {
        ReflectionTestUtils.setField(otpService, "random", random);
        when(random.nextInt(999999)).thenReturn(7);

        String otp = otpService.generateOtp(email);

        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }

    @Test
    void generateOtp_ShouldHandleZeroValue() {
        ReflectionTestUtils.setField(otpService, "random", random);
        when(random.nextInt(999999)).thenReturn(0);

        String otp = otpService.generateOtp(email);

        assertEquals("000000", otp);
    }
}