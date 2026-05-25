package com.config.code.dto;

import com.config.code.domain.CodeItem;

public record CodeItemResponse(
        Long id,
        String code,
        String name,
        int sortOrder,
        String useYn
) {
    public static CodeItemResponse from(CodeItem item) {
        return new CodeItemResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getSortOrder(),
                item.getUseYn()
        );
    }
}
