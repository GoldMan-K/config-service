package com.config.taxonomy.dto;

public record CreateCategoryRequest(
        String categoryCode,
        String categoryName,
        int sortOrder
) {}
