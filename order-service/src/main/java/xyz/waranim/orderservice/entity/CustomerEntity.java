package xyz.waranim.orderservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.waranim.common.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEntity extends BaseEntity {
    @Column(unique = true, nullable = false)
    private UUID authId;

    private String email;

    @Column(name = "full_name")
    private String fullName;
}
