package xyz.waranim.notificationservice.dto;

import java.util.Map;
import java.util.UUID;

public record ShopDto(
        UUID id,
        UUID ownerId,
        String slug,
        String name,
        String description,
        Map<String, Object> theme,
        Boolean isPublished
) { }
