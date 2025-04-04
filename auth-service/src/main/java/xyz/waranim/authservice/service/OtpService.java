package xyz.waranim.authservice.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.waranim.authservice.entity.OtpEntity;
import xyz.waranim.authservice.repository.OtpRepository;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpRepository otpRepository;
    private final Random random = new Random();

    @Getter
    @Value("${jwt.otp.expiration}")
    private Long expiration;

    public String generateOtp(String email) {
        String otp = String.format("%06d", random.nextInt(999999));
        OtpEntity otpEntity = new OtpEntity();
        otpEntity.setEmail(email);
        otpEntity.setCode(otp);
        otpEntity.setExpiration(expiration);
        otpRepository.save(otpEntity);

        // TODO: Заменить на отправку через SMTP
        System.out.println("OTP для " + email + ": " + otp);
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        return otpRepository.findById(email)
                .map(OtpEntity::getCode)
                .filter(storedOtp -> storedOtp.equals(otp))
                .isPresent();
    }
}
