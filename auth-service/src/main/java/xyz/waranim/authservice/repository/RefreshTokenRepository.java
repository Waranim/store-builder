package xyz.waranim.authservice.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xyz.waranim.authservice.entity.RefreshTokenEntity;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshTokenEntity, String> {
    Optional<RefreshTokenEntity> findByEmail(String email);
    void deleteByEmail(String email);
}
