package xyz.waranim.orderservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartStorageServiceTest {

    @Mock
    CartRepository repo;
    @Mock
    RedisTemplate<String,Object> redisTemplate;
    @Mock
    ProductClient productClient;

    @InjectMocks
    CartStorageService service;

    UUID customerId;
    UUID productId;
    ProductDto productDto;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        productId  = UUID.randomUUID();
        productDto = new ProductDto(
                productId, UUID.randomUUID(), "Demo", null, null,
                "Desc", new BigDecimal("10.00"), "SKU1", null, true
        );
        lenient().when(redisTemplate.expire(anyString(), any(Duration.class))).thenReturn(true);
    }

    @Test
    void getCart_whenNotExists_returnsEmpty() {
        when(repo.findById(customerId)).thenReturn(Optional.empty());

        CartDto dto = service.getCart(customerId);

        assertThat(dto.customerId()).isEqualTo(customerId);
        assertThat(dto.items()).isEmpty();
        assertThat(dto.total()).isEqualByComparingTo("0.00");
    }

    @Test
    void getCart_whenExists_returnsMappedDto() {
        CartEntity entity = new CartEntity(customerId, new HashMap<>(), BigDecimal.ZERO);
        entity.getItems().put(productId,
                new CartItemEntity(productId, "Demo", new BigDecimal("5.00"), 2));
        entity.setTotal(new BigDecimal("10.00"));
        when(repo.findById(customerId)).thenReturn(Optional.of(entity));

        CartDto dto = service.getCart(customerId);

        assertThat(dto.total()).isEqualByComparingTo("10.00");
        assertThat(dto.items()).containsKey(productId);
    }

    @Test
    void addItem_createsNewCartAndSetsTtl() {
        when(repo.findById(customerId)).thenReturn(Optional.empty());
        when(productClient.getById(productId)).thenReturn(productDto);

        AddItemDto add = new AddItemDto(productId, 3);
        CartDto dto = service.addItem(customerId, add);

        assertThat(dto.items()).hasSize(1);
        assertThat(dto.total()).isEqualByComparingTo("30.00");

        ArgumentCaptor<CartEntity> cap = ArgumentCaptor.forClass(CartEntity.class);
        verify(repo).save(cap.capture());
        CartEntity saved = cap.getValue();
        assertThat(saved.getItems().get(productId).getQty()).isEqualTo(3);

        verify(redisTemplate).expire("carts:" + customerId, Duration.ofHours(48));
    }

    @Test
    void addItem_existingCart_increasesQty() {
        CartEntity entity = new CartEntity(customerId, new HashMap<>(), BigDecimal.ZERO);
        entity.getItems().put(productId,
                new CartItemEntity(productId, "Demo", new BigDecimal("10.00"), 1));
        when(repo.findById(customerId)).thenReturn(Optional.of(entity));

        CartDto dto = service.addItem(customerId, new AddItemDto(productId, 2));

        assertThat(dto.items().get(productId).qty()).isEqualTo(3);
        assertThat(dto.total()).isEqualByComparingTo("30.00");
    }

    @Test
    void updateItem_changesQty() {
        CartEntity entity = new CartEntity(customerId, new HashMap<>(), BigDecimal.ZERO);
        entity.getItems().put(productId,
                new CartItemEntity(productId, "Demo", new BigDecimal("10.00"), 1));
        when(repo.findById(customerId)).thenReturn(Optional.of(entity));

        CartDto dto = service.updateItem(customerId, new UpdateItemDto(productId, 5));

        assertThat(dto.items().get(productId).qty()).isEqualTo(5);
        assertThat(dto.total()).isEqualByComparingTo("50.00");
    }

    @Test
    void updateItem_qtyZero_removesItem() {
        CartEntity entity = new CartEntity(customerId, new HashMap<>(), BigDecimal.ZERO);
        entity.getItems().put(productId,
                new CartItemEntity(productId, "Demo", new BigDecimal("10.00"), 1));
        when(repo.findById(customerId)).thenReturn(Optional.of(entity));

        CartDto dto = service.updateItem(customerId, new UpdateItemDto(productId, 0));

        assertThat(dto.items()).doesNotContainKey(productId);
        assertThat(dto.total()).isEqualByComparingTo("0.00");
    }

    @Test
    void removeItem_deletesPositionAndRecalculatesTotal() {
        // корзина с двумя товарами
        UUID otherId = UUID.randomUUID();
        Map<UUID, CartItemEntity> map = new HashMap<>();
        map.put(productId, new CartItemEntity(productId, "Demo", new BigDecimal("10.00"), 1));
        map.put(otherId,   new CartItemEntity(otherId,   "Other", new BigDecimal("5.00"), 2));
        CartEntity entity = new CartEntity(customerId, map, new BigDecimal("20.00"));
        when(repo.findById(customerId)).thenReturn(Optional.of(entity));

        service.removeItem(customerId, productId);

        assertThat(entity.getItems()).doesNotContainKey(productId);
        assertThat(entity.getTotal()).isEqualByComparingTo("10.00");
        verify(repo).save(entity);
        verify(redisTemplate).expire("carts:" + customerId, Duration.ofHours(48));
    }

    @Test
    void clearCart_deletesKey() {
        service.clearCart(customerId);
        verify(repo).deleteById(customerId);
        verifyNoMoreInteractions(repo);
    }
}