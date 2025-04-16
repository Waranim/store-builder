package xyz.waranim.authservice.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xyz.waranim.authservice.entity.OtpEntity;

@Repository
public interface OtpRepository extends CrudRepository<OtpEntity, String> {
}
