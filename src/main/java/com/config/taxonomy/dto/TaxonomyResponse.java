package com.config.taxonomy.dto;

import com.config.taxonomy.domain.Category;
import com.config.taxonomy.domain.Region;
import com.config.taxonomy.domain.SubCategory;

import java.util.List;

public record TaxonomyResponse(
        List<RegionDto> regions,
        List<CategoryDto> categories
) {
    public record RegionDto(String regionCode, String regionName, int sortOrder, String useYn) {
        public static RegionDto from(Region r) {
            return new RegionDto(r.getRegionCode(), r.getRegionName(), r.getSortOrder(), r.getUseYn());
        }
    }

    public record CategoryDto(
            String categoryCode,
            String categoryName,
            int sortOrder,
            String useYn,
            List<SubCategoryDto> subCategories
    ) {
        public static CategoryDto from(Category c, List<SubCategory> subs) {
            return new CategoryDto(
                    c.getCategoryCode(),
                    c.getCategoryName(),
                    c.getSortOrder(),
                    c.getUseYn(),
                    subs.stream().map(SubCategoryDto::from).toList()
            );
        }
    }

    public record SubCategoryDto(
            String subCategoryCode,
            String subCategoryName,
            int sortOrder,
            String useYn
    ) {
        public static SubCategoryDto from(SubCategory s) {
            return new SubCategoryDto(
                    s.getSubCategoryCode(),
                    s.getSubCategoryName(),
                    s.getSortOrder(),
                    s.getUseYn()
            );
        }
    }
}
