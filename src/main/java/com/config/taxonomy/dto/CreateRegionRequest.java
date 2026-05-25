package com.config.taxonomy.dto;

public record CreateRegionRequest(
        String regionCode,
        String regionName,
        int sortOrder
) {}
