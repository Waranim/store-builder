package xyz.waranim.orderservice.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.waranim.orderservice.dto.CreateOrderItemDto;
import xyz.waranim.orderservice.dto.OrderItemDto;
import xyz.waranim.orderservice.entity.OrderEntity;
import xyz.waranim.orderservice.entity.OrderItemEntity;
import xyz.waranim.orderservice.repository.OrderItemRepository;
import xyz.waranim.orderservice.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository itemRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderItemService itemService;

    @Test
    void create_ShouldThrow_WhenOrderNotFound() {
        UUID orderId = UUID.randomUUID();
        CreateOrderItemDto dto = new CreateOrderItemDto(UUID.randomUUID(), "Товар", BigDecimal.valueOf(100), 2);
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.create(orderId, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Заказ не найден: " + orderId);
    }

    @Test
    void create_ShouldSaveAndReturnDto_WhenOrderExists() {
        UUID orderId = UUID.randomUUID();
        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        CreateOrderItemDto dto = new CreateOrderItemDto(UUID.randomUUID(), "Товар", BigDecimal.valueOf(100), 2);
        OrderItemEntity saved = new OrderItemEntity();
        saved.setId(UUID.randomUUID());
        saved.setOrderEntity(order);
        saved.setProductId(dto.productId());
        saved.setProductName(dto.productName());
        saved.setUnitPrice(dto.unitPrice());
        saved.setQty(dto.qty());
        when(itemRepository.save(any(OrderItemEntity.class))).thenReturn(saved);

        OrderItemDto result = itemService.create(orderId, dto);

        assertThat(result.id()).isEqualTo(saved.getId());
        assertThat(result.productName()).isEqualTo("Товар");

        ArgumentCaptor<OrderItemEntity> cap = ArgumentCaptor.forClass(OrderItemEntity.class);
        verify(itemRepository).save(cap.capture());
        assertThat(cap.getValue().getOrderEntity()).isEqualTo(order);
    }

    @Test
    void getById_ShouldReturnDto_WhenFound() {
        UUID id = UUID.randomUUID();
        OrderItemEntity ent = new OrderItemEntity();
        ent.setId(id);
        when(itemRepository.findById(id)).thenReturn(Optional.of(ent));

        OrderItemDto dto = itemService.getById(id);

        assertThat(dto.id()).isEqualTo(id);
    }

    @Test
    void getById_ShouldThrow_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Товар не найден: " + id);
    }

    @Test
    void getByOrderId_ShouldReturnListOfDtos() {
        UUID orderId = UUID.randomUUID();
        OrderItemEntity e1 = new OrderItemEntity(), e2 = new OrderItemEntity();
        e1.setId(UUID.randomUUID()); e2.setId(UUID.randomUUID());
        when(itemRepository.findByOrderEntityId(orderId)).thenReturn(List.of(e1, e2));

        List<OrderItemDto> list = itemService.getByOrderId(orderId);

        assertThat(list).hasSize(2)
                .extracting(OrderItemDto::id)
                .containsExactly(e1.getId(), e2.getId());
    }

    @Test
    void update_ShouldModifyAndReturnDto_WhenExists() {
        UUID id = UUID.randomUUID();
        CreateOrderItemDto dto = new CreateOrderItemDto(UUID.randomUUID(), "Новый", BigDecimal.valueOf(200), 5);

        OrderItemEntity existing = new OrderItemEntity();
        existing.setId(id);
        existing.setProductName("Старый");
        when(itemRepository.findById(id)).thenReturn(Optional.of(existing));

        OrderItemEntity saved = new OrderItemEntity();
        saved.setId(id);
        saved.setProductName(dto.productName());
        saved.setUnitPrice(dto.unitPrice());
        saved.setQty(dto.qty());
        when(itemRepository.save(existing)).thenReturn(saved);

        OrderItemDto result = itemService.update(id, dto);

        assertThat(result.productName()).isEqualTo("Новый");
        assertThat(result.qty()).isEqualTo(5);
    }

    @Test
    void update_ShouldThrow_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.update(id, new CreateOrderItemDto(null,"",BigDecimal.ZERO,0)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Товар не найден: " + id);
    }

    @Test
    void delete_ShouldThrow_WhenNotExists() {
        UUID id = UUID.randomUUID();
        when(itemRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> itemService.delete(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Товар не найден: " + id);
    }

    @Test
    void delete_ShouldCallRepo_WhenExists() {
        UUID id = UUID.randomUUID();
        when(itemRepository.existsById(id)).thenReturn(true);

        itemService.delete(id);

        verify(itemRepository).deleteById(id);
    }
}