package com.config.code.dto;

public record UpdateCodeItemSortRequest(
        String code,
        int sortOrder
) {}
