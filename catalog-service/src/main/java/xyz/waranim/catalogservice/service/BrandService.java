package xyz.waranim.catalogservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import xyz.waranim.catalogservice.dto.BrandDto;
import xyz.waranim.catalogservice.dto.CreateBrand;
import xyz.waranim.catalogservice.entity.BrandEntity;
import xyz.waranim.catalogservice.repository.BrandRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandDto create(CreateBrand req) {
        if (brandRepository.findByNameIgnoreCase(req.name()).isPresent()) {
            throw new IllegalArgumentException("Бренд уже существует: " + req.name());
        }
        BrandEntity entity = new BrandEntity();
        entity.setName(req.name());
        entity.setLogoUrl(req.logoUrl());
        return BrandDto.of(brandRepository.save(entity));
    }

    public BrandDto get(UUID id) {
        return BrandDto.of(brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Бренд не найден: " + id)));
    }

    public Page<BrandDto> list(Pageable pageable) {
        return brandRepository.findAll(pageable).map(BrandDto::of);
    }

    public BrandDto update(UUID id, BrandDto incoming) {
        BrandEntity e = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Бренд не найден: " + id));
        e.setName(incoming.name());
        e.setLogoUrl(incoming.logoUrl());
        return BrandDto.of(brandRepository.save(e));
    }

    public void delete(UUID id) {
        brandRepository.deleteById(id);
    }
}
