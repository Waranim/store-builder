package xyz.waranim.authservice.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import xyz.waranim.authservice.entity.OtpEntity;
import xyz.waranim.authservice.repository.OtpRepository;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpRepository otpRepository;
    private final MailSender mailSender;
    private final Random random = new Random();

    private static final String SUBJECT_NAME = "Код для авторизации на платформе";
    private static final String TEMPLATE_MAIL_TEXT = "Для входа в аккаунт %s был сгенерирован код:%s\n" +
            "Код действителен 5 минут.";
    private static final String TEMPLATE_MAIL_FROM = "noreply@%s";

    @Getter
    @Value("${jwt.otp.expiration}")
    private Long expiration;

    @Value("${spring.mail.properties.from}")
    private String from;

    public String generateOtp(String email) {
        String otp = String.format("%06d", random.nextInt(999999));
        OtpEntity otpEntity = new OtpEntity();
        otpEntity.setEmail(email);
        otpEntity.setCode(otp);
        otpEntity.setExpiration(expiration);
        otpRepository.save(otpEntity);

        sendOtp(email, otp);
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        return otpRepository.findById(email)
                .map(OtpEntity::getCode)
                .filter(storedOtp -> storedOtp.equals(otp))
                .isPresent();
    }

    private void sendOtp(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(String.format(TEMPLATE_MAIL_FROM, from));
        message.setTo(email);
        message.setSubject(SUBJECT_NAME);
        message.setText(String.format(TEMPLATE_MAIL_TEXT, email, otp));
        mailSender.send(message);
    }
}
