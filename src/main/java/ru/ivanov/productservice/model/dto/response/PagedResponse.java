package ru.ivanov.productservice.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Paginated response")
public record PagedResponse<T>(
        @Schema(
                description = "Zero-based page number",
                example = "0",
                minimum = "0"
        )
        int pageNumber,
        @Schema(
                description = "Number of items per page",
                example = "10",
                minimum = "1"
        )
        int pageSize,
        @Schema(
                description = "Total number of elements across all pages",
                example = "42",
                minimum = "0"
        )
        long totalElements,
        @Schema(
                description = "Total number of pages available",
                example = "5",
                minimum = "0"
        )
        int totalPages,
        @Schema(
                description = "Indicates if this is the first page",
                example = "true"
        )
        boolean first,
        @Schema(
                description = "Indicates if this is the last page",
                example = "false"
        )
        boolean last,
        @Schema(
                description = "List of items for the current page",
                implementation = Object.class
        )
        List<T> content
) {
    public static <T> PagedResponse<T> fromPage(Page<T> page) {
        return new PagedResponse<>(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.getContent()
        );
    }
}