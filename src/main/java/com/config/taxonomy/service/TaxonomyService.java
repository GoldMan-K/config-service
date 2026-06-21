package com.config.taxonomy.service;

import com.config.global.exception.BusinessException;
import com.config.global.exception.ErrorCode;
import com.config.kafka.producer.ConfigEventProducer;
import com.config.taxonomy.domain.Category;
import com.config.taxonomy.domain.Region;
import com.config.taxonomy.domain.SubCategory;
import com.config.taxonomy.dto.*;
import com.config.taxonomy.repository.CategoryRepository;
import com.config.taxonomy.repository.RegionRepository;
import com.config.taxonomy.repository.SubCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaxonomyService {

    private static final String ACTIVE = "Y";

    private final RegionRepository regionRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ConfigEventProducer eventProducer;

    // ─── 전체 트리 조회 (CDN 캐싱 권장) ─────────────────────────────────────

    @Cacheable("taxonomy")
    public TaxonomyResponse getTaxonomy() {
        List<Region> regions = regionRepository.findAllByUseYnOrderBySortOrderAsc(ACTIVE);
        List<Category> categories = categoryRepository.findAllByUseYnOrderBySortOrderAsc(ACTIVE);

        List<TaxonomyResponse.RegionDto> regionDtos = regions.stream()
                .map(TaxonomyResponse.RegionDto::from)
                .toList();

        List<TaxonomyResponse.CategoryDto> categoryDtos = categories.stream()
                .map(c -> {
                    List<SubCategory> subs = subCategoryRepository
                            .findAllByCategoryCodeAndUseYnOrderBySortOrderAsc(c.getCategoryCode(), ACTIVE);
                    return TaxonomyResponse.CategoryDto.from(c, subs);
                })
                .toList();

        return new TaxonomyResponse(regionDtos, categoryDtos);
    }

    // ─── 지역 추가 [ADMIN] ───────────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = "taxonomy", allEntries = true)
    public void createRegion(CreateRegionRequest request) {
        if (regionRepository.existsByRegionCode(request.regionCode())) {
            throw new BusinessException(ErrorCode.REGION_ALREADY_EXISTS);
        }
        regionRepository.save(Region.builder()
                .regionCode(request.regionCode())
                .regionName(request.regionName())
                .sortOrder(request.sortOrder())
                .build());

        eventProducer.publishTaxonomyChanged("REGION_ADDED", request.regionCode());
    }

    // ─── 카테고리 추가 [ADMIN] ───────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = "taxonomy", allEntries = true)
    public void createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByCategoryCode(request.categoryCode())) {
            throw new BusinessException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }
        categoryRepository.save(Category.builder()
                .categoryCode(request.categoryCode())
                .categoryName(request.categoryName())
                .sortOrder(request.sortOrder())
                .build());

        eventProducer.publishTaxonomyChanged("CATEGORY_ADDED", request.categoryCode());
    }

    // ─── 서브카테고리 추가 [ADMIN] ───────────────────────────────────────────

    @Transactional
    @CacheEvict(value = "taxonomy", allEntries = true)
    public void createSubCategory(CreateSubCategoryRequest request) {
        if (!categoryRepository.existsByCategoryCode(request.categoryCode())) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        if (subCategoryRepository.existsByCategoryCodeAndSubCategoryCode(
                request.categoryCode(), request.subCategoryCode())) {
            throw new BusinessException(ErrorCode.SUB_CATEGORY_ALREADY_EXISTS);
        }
        subCategoryRepository.save(SubCategory.builder()
                .categoryCode(request.categoryCode())
                .subCategoryCode(request.subCategoryCode())
                .subCategoryName(request.subCategoryName())
                .sortOrder(request.sortOrder())
                .build());

        eventProducer.publishTaxonomyChanged("SUB_CATEGORY_ADDED", request.subCategoryCode());
    }

    // ─── 지역 비활성화 [ADMIN] ───────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = "taxonomy", allEntries = true)
    public void deactivateRegion(String regionCode) {
        Region region = regionRepository.findByRegionCode(regionCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.REGION_NOT_FOUND));

        if (!region.isActive()) {
            throw new BusinessException(ErrorCode.REGION_ALREADY_INACTIVE);
        }
        region.deactivate();
        eventProducer.publishTaxonomyChanged("REGION_DEACTIVATED", regionCode);
    }

    // ─── 카테고리 비활성화 [ADMIN] ───────────────────────────────────────────

    @Transactional
    @CacheEvict(value = "taxonomy", allEntries = true)
    public void deactivateCategory(String categoryCode) {
        Category category = categoryRepository.findByCategoryCode(categoryCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        if (!category.isActive()) {
            throw new BusinessException(ErrorCode.CATEGORY_ALREADY_INACTIVE);
        }
        category.deactivate();
        eventProducer.publishTaxonomyChanged("CATEGORY_DEACTIVATED", categoryCode);
    }
}
