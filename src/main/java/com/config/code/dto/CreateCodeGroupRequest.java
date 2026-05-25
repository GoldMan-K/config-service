package com.config.code.dto;

public record CreateCodeGroupRequest(
        String groupCode,
        String groupName,
        String description
) {}
