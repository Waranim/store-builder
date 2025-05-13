package xyz.waranim.orderservice.service;

import java.util.*;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.waranim.orderservice.dto.CreateCustomerDto;
import xyz.waranim.orderservice.dto.CustomerDto;
import xyz.waranim.orderservice.entity.CustomerEntity;
import xyz.waranim.orderservice.repository.CustomerRepository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void create_ShouldSaveAndReturnDto() {
        CreateCustomerDto dto = new CreateCustomerDto("email@example.com", "Иван Иванов");
        CustomerEntity saved = new CustomerEntity();
        saved.setId(UUID.randomUUID());
        saved.setEmail(dto.email());
        saved.setFullName(dto.fullName());
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(saved);

        CustomerDto result = customerService.create(dto);

        assertThat(result.id()).isEqualTo(saved.getId());
        assertThat(result.email()).isEqualTo("email@example.com");
        assertThat(result.fullName()).isEqualTo("Иван Иванов");

        ArgumentCaptor<CustomerEntity> captor = ArgumentCaptor.forClass(CustomerEntity.class);
        verify(customerRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo(dto.email());
        assertThat(captor.getValue().getFullName()).isEqualTo(dto.fullName());
    }

    @Test
    void getById_ShouldReturnDto_WhenFound() {
        UUID id = UUID.randomUUID();
        CustomerEntity ent = new CustomerEntity();
        ent.setId(id);
        ent.setEmail("a@b.com");
        ent.setFullName("Пётр Петров");
        when(customerRepository.findById(id)).thenReturn(Optional.of(ent));

        CustomerDto dto = customerService.getById(id);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.email()).isEqualTo("a@b.com");
    }

    @Test
    void getById_ShouldThrow_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Покупатель не найден: " + id);
    }

    @Test
    void getAll_ShouldReturnListOfDtos() {
        CustomerEntity e1 = new CustomerEntity(), e2 = new CustomerEntity();
        e1.setId(UUID.randomUUID()); e2.setId(UUID.randomUUID());
        e1.setEmail("e1"); e1.setFullName("f1");
        e2.setEmail("e2"); e2.setFullName("f2");
        when(customerRepository.findAll()).thenReturn(List.of(e1, e2));

        List<CustomerDto> list = customerService.getAll();

        assertThat(list).hasSize(2)
                .extracting(CustomerDto::id)
                .containsExactly(e1.getId(), e2.getId());
    }

    @Test
    void update_ShouldModifyAndReturnDto_WhenExists() {
        UUID id = UUID.randomUUID();
        CreateCustomerDto dto = new CreateCustomerDto("new@mail", "Новый Имя");
        CustomerEntity existing = new CustomerEntity();
        existing.setId(id);
        existing.setEmail("old@mail");
        existing.setFullName("Старое Имя");
        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));

        CustomerEntity saved = new CustomerEntity();
        saved.setId(id);
        saved.setEmail(dto.email());
        saved.setFullName(dto.fullName());
        when(customerRepository.save(existing)).thenReturn(saved);

        CustomerDto result = customerService.update(id, dto);

        assertThat(result.email()).isEqualTo("new@mail");
        assertThat(result.fullName()).isEqualTo("Новый Имя");
    }

    @Test
    void update_ShouldThrow_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.update(id, new CreateCustomerDto("a","b")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Покупатель не найден: " + id);
    }

    @Test
    void delete_ShouldThrow_WhenNotExists() {
        UUID id = UUID.randomUUID();
        when(customerRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> customerService.delete(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Покупатель не найден: " + id);
    }

    @Test
    void delete_ShouldCallRepo_WhenExists() {
        UUID id = UUID.randomUUID();
        when(customerRepository.existsById(id)).thenReturn(true);

        customerService.delete(id);

        verify(customerRepository).deleteById(id);
    }
}