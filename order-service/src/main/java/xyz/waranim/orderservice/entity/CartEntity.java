package xyz.waranim.orderservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RedisHash("carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartEntity implements Serializable {

    @Id
    private UUID customerId;

    private Map<UUID, CartItemEntity> items = new HashMap<>();

    private BigDecimal total = BigDecimal.ZERO;
}
