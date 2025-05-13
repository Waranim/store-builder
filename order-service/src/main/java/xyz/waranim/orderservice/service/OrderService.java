package xyz.waranim.orderservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.waranim.orderservice.dto.CreateOrderDto;
import xyz.waranim.orderservice.dto.CreateOrderItemDto;
import xyz.waranim.orderservice.dto.OrderDto;
import xyz.waranim.orderservice.entity.CustomerEntity;
import xyz.waranim.orderservice.entity.OrderEntity;
import xyz.waranim.orderservice.entity.OrderItemEntity;
import xyz.waranim.orderservice.entity.OrderStatus;
import xyz.waranim.orderservice.repository.CustomerRepository;
import xyz.waranim.orderservice.repository.OrderRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public OrderDto create(CreateOrderDto dto) {
        CustomerEntity customer = customerRepository.findById(dto.customerId())
                .orElseThrow(() -> new EntityNotFoundException("Покупатель не найден: " + dto.customerId()));

        OrderEntity order = new OrderEntity();
        order.setCustomer(customer);
        order.setShopId(dto.shopId());
        order.setItems(dto.items().stream().map(this::toEntity).toList());

        order = orderRepository.save(order);
        return OrderDto.of(order);
    }

    public OrderDto getById(UUID id) {
        return orderRepository.findById(id)
                .map(OrderDto::of)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден: " + id));
    }

    public List<OrderDto> getByShopId(UUID shopId) {
        return orderRepository.findByShopId(shopId)
                .stream()
                .map(OrderDto::of)
                .toList();
    }

    public List<OrderDto> getByCustomerId(UUID customerId) {
        return orderRepository.findByCustomerId(customerId)
                .stream()
                .map(OrderDto::of)
                .toList();
    }

    public List<OrderDto> getAll() {
        return orderRepository.findAll()
                .stream()
                .map(OrderDto::of)
                .toList();
    }

    public OrderDto updateStatus(UUID id, OrderStatus status) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден: " + id));
        order.setStatus(status);
        order = orderRepository.save(order);
        return OrderDto.of(order);
    }

    public void delete(UUID id) {
        if (!orderRepository.existsById(id)) {
            throw new EntityNotFoundException("Заказ не найден: " + id);
        }
        orderRepository.deleteById(id);
    }

    private OrderItemEntity toEntity(CreateOrderItemDto dto) {
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setProductId(dto.productId());
        orderItem.setProductName(dto.productName());
        orderItem.setUnitPrice(dto.unitPrice());
        orderItem.setQty(dto.qty());

        return orderItem;
    }
}
