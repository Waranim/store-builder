package xyz.waranim.shopservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.waranim.shopservice.dto.CreateShop;
import xyz.waranim.shopservice.dto.ShopDto;
import xyz.waranim.shopservice.entity.ShopEntity;
import xyz.waranim.shopservice.repository.ShopRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShopService {
    private final ShopRepository shopRepository;

    public ShopDto create(CreateShop createShop, String ownerId) {
        if (shopRepository.findBySlug(createShop.slug()).isPresent()) {
            throw new IllegalArgumentException("Slug уже занят: " + createShop.slug());
        }
        ShopEntity shopEntity = new ShopEntity();
        shopEntity.setOwnerId(UUID.fromString(ownerId));
        shopEntity.setSlug(createShop.slug());
        shopEntity.setName(createShop.name());
        shopEntity.setDescription(createShop.description());
        shopEntity = shopRepository.save(shopEntity);

        return ShopDto.of(shopEntity);
    }

    public ShopDto getById(UUID id) {
        return ShopDto.of(get(id));
    }

    private ShopEntity get(UUID id) {
        return shopRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Магазин не найден: " + id));
    }

    public List<ShopDto> getAll() {
        return shopRepository.findAll().stream().map(ShopDto::of).toList();
    }

    public ShopDto update(UUID id, ShopDto incoming) {
        ShopEntity existing = get(id);
        existing.setName(incoming.name());
        existing.setDescription(incoming.description());
        existing.setTheme(incoming.theme());
        existing.setIsPublished(incoming.isPublished());

        return ShopDto.of(shopRepository.save(existing));
    }

    public ShopDto updateBySlug(UUID id, String slug) {
        ShopEntity existing = get(id);
        existing.setSlug(slug);

        return ShopDto.of(shopRepository.save(existing));
    }

    public void delete(UUID id) {
        if (!shopRepository.existsById(id)) {
            throw new EntityNotFoundException("Магазин не найден: " + id);
        }
        shopRepository.deleteById(id);
    }
}
