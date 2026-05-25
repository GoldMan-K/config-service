package com.config.code.dto;

public record CreateCodeItemRequest(
        String groupCode,
        String code,
        String name,
        int sortOrder
) {}
