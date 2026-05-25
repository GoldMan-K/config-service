package com.config.code.dto;

public record UpdateCodeItemRequest(
        String name,
        int sortOrder,
        String useYn   // Y | N
) {}
