package ru.ivanov.productservice.model.dto;

import java.util.UUID;

public record ProductDto(
        UUID id,
        String title,
        String details
) {
}
