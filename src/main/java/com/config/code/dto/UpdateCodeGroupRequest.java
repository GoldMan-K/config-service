package com.config.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateCodeGroupRequest(
        @NotBlank String groupName,
        String description,
        @Pattern(regexp = "[YN]", message = "useYn은 Y 또는 N이어야 합니다.") String useYn   // Y | N
) {}

