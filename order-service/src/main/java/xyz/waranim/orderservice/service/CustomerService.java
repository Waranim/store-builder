package xyz.waranim.orderservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.waranim.orderservice.dto.CreateCustomerDto;
import xyz.waranim.orderservice.dto.CustomerDto;
import xyz.waranim.orderservice.entity.CustomerEntity;
import xyz.waranim.orderservice.repository.CustomerRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerDto create(CreateCustomerDto dto) {
        CustomerEntity entity = new CustomerEntity();
        entity.setEmail(dto.email());
        entity.setFullName(dto.fullName());

        entity = customerRepository.save(entity);
        return CustomerDto.of(entity);
    }

    public CustomerDto getById(UUID id) {
        return customerRepository.findById(id)
                .map(CustomerDto::of)
                .orElseThrow(() -> new EntityNotFoundException("Покупатель не найден: " + id));
    }

    public List<CustomerDto> getAll() {
        return customerRepository.findAll()
                .stream()
                .map(CustomerDto::of)
                .toList();
    }

    public CustomerDto update(UUID id, CreateCustomerDto dto) {
        CustomerEntity entity = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Покупатель не найден: " + id));
        entity.setEmail(dto.email());
        entity.setFullName(dto.fullName());
        entity = customerRepository.save(entity);

        return CustomerDto.of(entity);
    }

    public void delete(UUID id) {
        if (!customerRepository.existsById(id)) {
            throw new EntityNotFoundException("Покупатель не найден: " + id);
        }
        customerRepository.deleteById(id);
    }
}
