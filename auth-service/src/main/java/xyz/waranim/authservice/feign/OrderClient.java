package xyz.waranim.authservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import xyz.waranim.authservice.dto.CreateCustomerDto;

import java.util.UUID;

@FeignClient(
        name = "order-client",
        url = "${services.order.url}"
)
public interface OrderClient {

    @PostMapping("/api/v1/order/customers/create")
    void createCustomer(@RequestHeader("X-User-Id") UUID userId, @RequestBody CreateCustomerDto customer);
}
