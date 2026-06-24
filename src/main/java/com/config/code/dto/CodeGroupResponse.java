package com.config.code.dto;

import com.config.code.domain.CodeGroup;

public record CodeGroupResponse(
        String groupCode,
        String groupName,
        String description,
        String useYn
) {
    public static CodeGroupResponse from(CodeGroup group) {
        return new CodeGroupResponse(
                group.getGroupCode(),
                group.getGroupName(),
                group.getDescription(),
                group.getUseYn()
        );
    }
}

