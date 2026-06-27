package com.config.code.dto;

import com.config.code.domain.CodeItem;

import java.time.LocalDateTime;

public record CodeItemResponse(
        Long id,
        String code,
        String name,
        int sortOrder,
        String useYn,
        LocalDateTime updatedAt
) {
    public static CodeItemResponse from(CodeItem item) {
        return new CodeItemResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getSortOrder(),
                item.getUseYn(),
                item.getUpdatedAt()
        );
    }
}
