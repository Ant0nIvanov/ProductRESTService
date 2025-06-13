package ru.ivanov.productservice.model.dto.request;

public record UpdateProductRequest(
        String title,
        String details
) {
}
