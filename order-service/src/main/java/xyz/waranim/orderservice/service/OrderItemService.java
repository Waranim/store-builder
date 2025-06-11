package xyz.waranim.orderservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.waranim.orderservice.dto.CreateOrderItemDto;
import xyz.waranim.orderservice.dto.OrderItemDto;
import xyz.waranim.orderservice.entity.OrderEntity;
import xyz.waranim.orderservice.entity.OrderItemEntity;
import xyz.waranim.orderservice.repository.OrderItemRepository;
import xyz.waranim.orderservice.repository.OrderRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderItemRepository itemRepository;
    private final OrderRepository orderRepository;

    public OrderItemDto create(UUID orderId, CreateOrderItemDto dto) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден: " + orderId));

        OrderItemEntity entity = new OrderItemEntity();
        entity.setOrderEntity(order);
        entity.setProductId(dto.productId());
        entity.setProductName(dto.productName());
        entity.setUnitPrice(dto.unitPrice());
        entity.setQty(dto.qty());

        entity = itemRepository.save(entity);
        return OrderItemDto.of(entity);
    }

    public OrderItemDto getById(UUID id) {
        return itemRepository.findById(id)
                .map(OrderItemDto::of)
                .orElseThrow(() -> new EntityNotFoundException("Товар не найден: " + id));
    }

    public List<OrderItemDto> getByOrderId(UUID orderId) {
        return itemRepository.findByOrderEntityId(orderId)
                .stream()
                .map(OrderItemDto::of)
                .toList();
    }

    public OrderItemDto update(UUID id, CreateOrderItemDto dto) {
        OrderItemEntity entity = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Товар не найден: " + id));
        entity.setProductId(dto.productId());
        entity.setProductName(dto.productName());
        entity.setUnitPrice(dto.unitPrice());
        entity.setQty(dto.qty());
        entity = itemRepository.save(entity);
        return OrderItemDto.of(entity);
    }

    public void delete(UUID id) {
        if (!itemRepository.existsById(id)) {
            throw new EntityNotFoundException("Товар не найден: " + id);
        }
        itemRepository.deleteById(id);
    }
}
