package xyz.waranim.notificationservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import xyz.waranim.notificationservice.dto.UserResponse;

import java.util.UUID;

@FeignClient(
        name = "auth-client",
        url = "${services.auth.url}"
)
public interface AuthClient {

    @GetMapping("/api/v1/auth/{id}")
    UserResponse getUser(@PathVariable("id") UUID id);
}
