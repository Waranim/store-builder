package xyz.waranim.orderservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import xyz.waranim.orderservice.dto.AddItemDto;
import xyz.waranim.orderservice.dto.CartDto;
import xyz.waranim.orderservice.dto.ProductDto;
import xyz.waranim.orderservice.dto.UpdateItemDto;
import xyz.waranim.orderservice.entity.CartEntity;
import xyz.waranim.orderservice.entity.CartItemEntity;
import xyz.waranim.orderservice.feign.ProductClient;
import xyz.waranim.orderservice.repository.CartRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartStorageService {

    private final CartRepository repo;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductClient productClient;

    private static final Duration TTL = Duration.ofHours(48);

    public CartDto getCart(UUID customerId) {
        return repo.findById(customerId)
                .map(CartDto::toDto)
                .orElse(new CartDto(customerId, Map.of(), BigDecimal.ZERO));
    }

    public CartDto addItem(UUID customerId, AddItemDto dto) {

        CartEntity cart = repo.findById(customerId)
                .orElse(new CartEntity(customerId, new HashMap<>(), BigDecimal.ZERO));

        CartItemEntity item = cart.getItems()
                .computeIfAbsent(dto.productId(), pid -> {
                    ProductDto p = productClient.getById(pid);
                    return new CartItemEntity(
                            pid,
                            p.name(),
                            p.price(),
                            0
                    );
                });

        item.setQty(item.getQty() + dto.qty());

        cart.setTotal(recalcTotal(cart));

        repo.save(cart);
        setTtl(customerId);

        return CartDto.toDto(cart);
    }

    public CartDto updateItem(UUID customerId, UpdateItemDto dto) {

        CartEntity cart = repo.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Корзина не найдена"));

        CartItemEntity item = cart.getItems().get(dto.productId());
        if (item == null)
            throw new EntityNotFoundException("Товар в корзине не найден");

        if (dto.qty() <= 0) {
            cart.getItems().remove(dto.productId());
        } else {
            item.setQty(dto.qty());
        }

        cart.setTotal(recalcTotal(cart));
        repo.save(cart);
        setTtl(customerId);

        return CartDto.toDto(cart);
    }

    public void removeItem(UUID customerId, UUID productId) {

        CartEntity cart = repo.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Корзина не найдена"));

        cart.getItems().remove(productId);

        cart.setTotal(recalcTotal(cart));
        repo.save(cart);
        setTtl(customerId);
    }

    public void clearCart(UUID customerId) {
        repo.deleteById(customerId);
    }

    private void setTtl(UUID customerId) {
        String key = "carts:" + customerId;
        redisTemplate.expire(key, TTL);
    }

    private BigDecimal recalcTotal(CartEntity cart) {
        return cart.getItems().values().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
