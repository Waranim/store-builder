package xyz.waranim.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.waranim.orderservice.entity.OrderItemEntity;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {
    List<OrderItemEntity> findByOrderEntityId(UUID orderId);
}
