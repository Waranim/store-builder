package xyz.waranim.orderservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import xyz.waranim.common.kafka.OrderStatus;
import xyz.waranim.common.kafka.OrderStatusEvent;
import xyz.waranim.orderservice.dto.CreateOrderDto;
import xyz.waranim.orderservice.dto.CreateOrderItemDto;
import xyz.waranim.orderservice.dto.OrderDto;
import xyz.waranim.orderservice.entity.CustomerEntity;
import xyz.waranim.orderservice.entity.OrderEntity;
import xyz.waranim.orderservice.entity.OrderItemEntity;
import xyz.waranim.orderservice.feign.ProductClient;
import xyz.waranim.orderservice.repository.CustomerRepository;
import xyz.waranim.orderservice.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final KafkaTemplate<String, OrderStatusEvent> kafka;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductClient productClient;

    public OrderDto create(CreateOrderDto dto) {
        CustomerEntity customer = customerRepository.findById(dto.customerId())
                .orElseThrow(() -> new EntityNotFoundException("Покупатель не найден: " + dto.customerId()));

        OrderEntity order = new OrderEntity();
        order.setCustomer(customer);
        order.setShopId(dto.shopId());

        BigDecimal total = BigDecimal.ZERO;

        for (CreateOrderItemDto itemDto : dto.items()) {
            OrderItemEntity item = toEntity(itemDto);
            order.addItem(item);
            productClient.subtractQuantity(itemDto.productId(), itemDto.qty());

            total = total.add(item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQty())));
        }

        order.setTotal(total);

        order = orderRepository.save(order);
        publish(order, OrderStatus.NEW);
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
        publish(order, status);
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

    private void publish(OrderEntity order, OrderStatus status) {
        OrderStatusEvent event = new OrderStatusEvent(
                order.getId().toString(), order.getShopId().toString(), order.getCustomer().getEmail(),
                status, Instant.now()
        );
        kafka.send("order.status.changed", order.getId().toString(), event);
    }
}
