package xyz.waranim.mediaservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.waranim.common.BaseEntity;

@Entity
@Table(name = "media", schema = "media")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MediaEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String objectKey;

    private String originalName;

    private String contentType;

    private Long size;
}
