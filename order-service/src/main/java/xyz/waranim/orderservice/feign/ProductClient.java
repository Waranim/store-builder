package xyz.waranim.orderservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import xyz.waranim.orderservice.dto.ProductDto;

import java.util.UUID;

@FeignClient(
        name = "product-client",
        url  = "${services.product.url}"
)
public interface ProductClient {

    @GetMapping("/api/v1/product/{id}")
    ProductDto getById(@PathVariable UUID id);

    @PatchMapping("/api/v1/product/{id}/{count}")
    ProductDto updateQty(@PathVariable UUID id, @PathVariable Integer count);

    @PatchMapping("/api/v1/product/{id}/decrease/{delta}")
    ProductDto subtractQuantity(@PathVariable UUID id, @PathVariable Integer delta);
}
