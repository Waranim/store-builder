package xyz.waranim.authservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

@RedisHash("OTP")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpEntity {
    @Id
    private String email;

    private String code;

    @TimeToLive(unit = TimeUnit.SECONDS)
    private Long expiration;
}
