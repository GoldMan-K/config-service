package com.config.taxonomy.dto;

public record CreateSubCategoryRequest(
        String categoryCode,
        String subCategoryCode,
        String subCategoryName,
        int sortOrder
) {}
