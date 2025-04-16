package xyz.waranim.authservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

@Data
@RedisHash("RefreshToken")
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenEntity {

    @Id
    private String token;

    private String email;

    @TimeToLive(unit = TimeUnit.SECONDS)
    private Long expiration;
}
