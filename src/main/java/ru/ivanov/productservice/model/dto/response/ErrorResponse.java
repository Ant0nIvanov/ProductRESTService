package ru.ivanov.productservice.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
@Schema(description = "Error response")
public record ErrorResponse(
        @Schema(description = "Request path", example = "/api/v1/products/e1d797bb-26fe-4b58-8bcd-d9cdfa9eb263")
        String path,
        @Schema(description = "Error message", example = "Product not found with id = e1d797bb-26fe-4b58-8bcd-d9cdfa9eb263")
        String message,
        @Schema(description = "Http status code", example = "404")
        int statusCode,
        @Schema(description = "Timestamp when error occurred",
                example = "2023-05-15T12:34:56.789",
                type = "string",
                format = "date-time")
        LocalDateTime timestamp
) {
}