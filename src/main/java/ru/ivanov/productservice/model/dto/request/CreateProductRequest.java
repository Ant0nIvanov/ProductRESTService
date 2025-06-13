package ru.ivanov.productservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductRequest(
        @NotNull(message = "title не должно быть null")
        @NotBlank(message = "title не должно быть пустым")
        String title,

        @NotNull(message = "details не должно быть null")
        @NotBlank(message = "details не должно быть пустым")
        String details
) {
}
