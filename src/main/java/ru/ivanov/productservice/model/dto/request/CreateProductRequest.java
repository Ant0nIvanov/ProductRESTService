package ru.ivanov.productservice.model.dto.request;

public record CreateProductRequest(
        String title,
        String details
) {
}
