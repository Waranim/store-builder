package xyz.waranim.orderservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemEntity implements Serializable {

    private UUID productId;
    private String productName;
    private BigDecimal unitPrice;
    private int qty;
}
