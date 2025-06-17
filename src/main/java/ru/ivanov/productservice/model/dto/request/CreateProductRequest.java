package ru.ivanov.productservice.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data for create new product ")
public record CreateProductRequest(
        @NotNull(message = "title не должно быть null")
        @NotBlank(message = "title не должно быть пустым")
        @Schema(description = "Product title", example = "Water")
        String title,

        @NotNull(message = "details не должно быть null")
        @NotBlank(message = "details не должно быть пустым")
        @Schema(description = "Product details", example = "The best water in the world")
        String details
) {
}
