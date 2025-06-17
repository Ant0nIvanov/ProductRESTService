package ru.ivanov.productservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Product representing object")
public record ProductDto(
        @Schema(description = "ID of the product", example = "9b63c77d-8e91-4f40-adb1-6817b92081ab", type = "string", format = "uuid")
        UUID id,
        @Schema(description = "Title of the product", example = "Water")
        String title,
        @Schema(description = "Additional information about the product", example = "Best water in the world")
        String details
) {
}