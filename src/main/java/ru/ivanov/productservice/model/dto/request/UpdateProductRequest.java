package ru.ivanov.productservice.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Product data for update")
public record UpdateProductRequest(
        @NotNull(message = "title не должно быть null")
        @NotBlank(message = "title не должно быть пустым")
        @Schema(description = "Product title", example = "Milk")
        String title,

        @NotNull(message = "details не должно быть null")
        @NotBlank(message = "details не должно быть пустым")
        @Schema(description = "Product title", example = "The best milk in the world")
        String details
) {
}