package xyz.waranim.orderservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.waranim.common.BaseEntity;
import xyz.waranim.common.kafka.OrderStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity extends BaseEntity {

    @Column(name = "shop_id", nullable = false)
    private UUID shopId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.NEW;

    @Column(nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(
            mappedBy = "orderEntity",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<OrderItemEntity> items = new ArrayList<>();

    public void addItem(OrderItemEntity item) {
        items.add(item);
        item.setOrderEntity(this);
    }

    public void removeItem(OrderItemEntity item) {
        items.remove(item);
        item.setOrderEntity(null);
    }
}
