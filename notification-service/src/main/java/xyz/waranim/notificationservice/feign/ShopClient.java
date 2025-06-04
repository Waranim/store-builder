package xyz.waranim.notificationservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import xyz.waranim.notificationservice.dto.ShopDto;

import java.util.UUID;

@FeignClient(
        name = "shop-client",
        url = "${services.shop.url}"
)
public interface ShopClient {

    @GetMapping("/api/v1/shop/{id}")
    ShopDto getById(@PathVariable UUID id);
}
