package com.thelineage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Schema(description = "Paginated response wrapper.")
public record PageResponse<T>(
        @Schema(description = "Current page (zero-indexed).", example = "0") int page,
        @Schema(description = "Page size (items per page).", example = "20") int size,
        @Schema(description = "Total items across all pages.", example = "143") long totalElements,
        @Schema(description = "Total page count.", example = "8") int totalPages,
        @Schema(description = "Items in this page.") List<T> content
) {
    public static <S, T> PageResponse<T> from(Page<S> page, Function<S, T> mapper) {
        return new PageResponse<>(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getContent().stream().map(mapper).toList()
        );
    }
}
