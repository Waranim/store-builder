package xyz.waranim.orderservice.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import xyz.waranim.common.kafka.OrderStatus;
import xyz.waranim.common.kafka.OrderStatusEvent;
import xyz.waranim.orderservice.dto.CreateOrderDto;
import xyz.waranim.orderservice.dto.CustomerDto;
import xyz.waranim.orderservice.dto.OrderDto;
import xyz.waranim.orderservice.entity.CustomerEntity;
import xyz.waranim.orderservice.entity.OrderEntity;
import xyz.waranim.orderservice.repository.CustomerRepository;
import xyz.waranim.orderservice.repository.OrderRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private KafkaTemplate<String, OrderStatusEvent> kafka;

    @InjectMocks
    private OrderService orderService;

    @Test
    void create_ShouldThrow_WhenCustomerNotFound() {
        when(customerRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        CreateOrderDto dto = new CreateOrderDto(UUID.randomUUID(), UUID.randomUUID(), List.of());
        assertThatThrownBy(() -> orderService.create(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Покупатель не найден: " + dto.customerId());
    }

    @Test
    void create_ShouldSaveAndReturnDto_WhenCustomerExists() {
        UUID custId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();

        CustomerEntity cust = new CustomerEntity();
        cust.setId(custId);
        when(customerRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(cust));

        OrderEntity saved = new OrderEntity();
        saved.setId(UUID.randomUUID());
        saved.setCustomer(cust);
        saved.setShopId(shopId);
        when(orderRepository.save(any(OrderEntity.class)))
                .thenReturn(saved);

        CreateOrderDto dto = new CreateOrderDto(shopId, custId, List.of());
        OrderDto result = orderService.create(dto);

        assertThat(result.id()).isEqualTo(saved.getId());
        assertThat(result.customer().id()).isEqualTo(custId);
        assertThat(result.shopId()).isEqualTo(shopId);

        ArgumentCaptor<OrderEntity> cap = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(cap.capture());
        assertThat(cap.getValue().getCustomer()).isEqualTo(cust);
        assertThat(cap.getValue().getShopId()).isEqualTo(shopId);
    }

    @Test
    void getById_ShouldReturnDto_WhenFound() {
        UUID id = UUID.randomUUID();
        UUID custId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();

        CustomerEntity cust = new CustomerEntity();
        cust.setId(custId);

        OrderEntity ent = new OrderEntity();
        ent.setId(id);
        ent.setCustomer(cust);
        ent.setShopId(shopId);

        when(orderRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(ent));

        OrderDto dto = orderService.getById(id);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.customer().id()).isEqualTo(custId);
        assertThat(dto.shopId()).isEqualTo(shopId);
    }

    @Test
    void getById_ShouldThrow_WhenNotFound() {
        when(orderRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getById(UUID.randomUUID()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageStartingWith("Заказ не найден:");
    }

    @Test
    void getByShopId_ShouldReturnList() {
        UUID shopId = UUID.randomUUID();
        UUID custId = UUID.randomUUID();

        CustomerEntity cust = new CustomerEntity();
        cust.setId(custId);

        OrderEntity o1 = new OrderEntity();
        o1.setId(UUID.randomUUID());
        o1.setCustomer(cust);
        o1.setShopId(shopId);

        OrderEntity o2 = new OrderEntity();
        o2.setId(UUID.randomUUID());
        o2.setCustomer(cust);
        o2.setShopId(shopId);

        when(orderRepository.findByShopId(any(UUID.class)))
                .thenReturn(List.of(o1, o2));

        var list = orderService.getByShopId(shopId);
        assertThat(list).hasSize(2)
                .extracting(OrderDto::shopId)
                .allMatch(s -> s.equals(shopId));
    }

    @Test
    void getByCustomerId_ShouldReturnList() {
        UUID shopId = UUID.randomUUID();
        UUID custId = UUID.randomUUID();

        CustomerEntity cust = new CustomerEntity();
        cust.setId(custId);

        OrderEntity o1 = new OrderEntity();
        o1.setId(UUID.randomUUID());
        o1.setCustomer(cust);
        o1.setShopId(shopId);

        OrderEntity o2 = new OrderEntity();
        o2.setId(UUID.randomUUID());
        o2.setCustomer(cust);
        o2.setShopId(shopId);

        when(orderRepository.findByCustomerId(any(UUID.class)))
                .thenReturn(List.of(o1, o2));

        var list = orderService.getByCustomerId(custId);
        assertThat(list).hasSize(2)
                .extracting(OrderDto::customer)
                .extracting(CustomerDto::id)
                .allMatch(id -> id.equals(custId));
    }

    @Test
    void getAll_ShouldReturnAll() {
        UUID shopId = UUID.randomUUID();
        UUID custId = UUID.randomUUID();

        CustomerEntity cust = new CustomerEntity();
        cust.setId(custId);

        OrderEntity o1 = new OrderEntity();
        o1.setId(UUID.randomUUID());
        o1.setCustomer(cust);
        o1.setShopId(shopId);

        OrderEntity o2 = new OrderEntity();
        o2.setId(UUID.randomUUID());
        o2.setCustomer(cust);
        o2.setShopId(shopId);

        when(orderRepository.findAll())
                .thenReturn(List.of(o1, o2));

        var list = orderService.getAll();
        assertThat(list).hasSize(2);
    }

    @Test
    void updateStatus_ShouldModifyAndReturnDto_WhenExists() {
        UUID id = UUID.randomUUID();
        UUID custId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();

        CustomerEntity cust = new CustomerEntity();
        cust.setId(custId);

        OrderEntity existing = new OrderEntity();
        existing.setId(id);
        existing.setCustomer(cust);
        existing.setShopId(shopId);
        existing.setStatus(OrderStatus.NEW);

        when(orderRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(existing));

        OrderEntity updated = new OrderEntity();
        updated.setId(id);
        updated.setCustomer(cust);
        updated.setShopId(shopId);
        updated.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.save(any(OrderEntity.class)))
                .thenReturn(updated);

        OrderDto dto = orderService.updateStatus(id, OrderStatus.SHIPPED);
        assertThat(dto.status()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(dto.customer().id()).isEqualTo(custId);
        assertThat(dto.shopId()).isEqualTo(shopId);
    }

    @Test
    void updateStatus_ShouldThrow_WhenNotFound() {
        when(orderRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateStatus(UUID.randomUUID(), OrderStatus.CANCELLED))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageStartingWith("Заказ не найден:");
    }

    @Test
    void delete_ShouldThrow_WhenNotExists() {
        when(orderRepository.existsById(any(UUID.class)))
                .thenReturn(false);

        assertThatThrownBy(() -> orderService.delete(UUID.randomUUID()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageStartingWith("Заказ не найден:");
    }

    @Test
    void delete_ShouldCallRepo_WhenExists() {
        when(orderRepository.existsById(any(UUID.class)))
                .thenReturn(true);

        orderService.delete(UUID.randomUUID());

        verify(orderRepository).deleteById(any(UUID.class));
    }
}