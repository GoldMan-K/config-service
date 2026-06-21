package com.config.taxonomy.service;

import com.config.global.exception.BusinessException;
import com.config.global.exception.ErrorCode;
import com.config.kafka.producer.ConfigEventProducer;
import com.config.taxonomy.domain.Category;
import com.config.taxonomy.domain.Region;
import com.config.taxonomy.domain.SubCategory;
import com.config.taxonomy.dto.TaxonomyResponse;
import com.config.taxonomy.repository.CategoryRepository;
import com.config.taxonomy.repository.RegionRepository;
import com.config.taxonomy.repository.SubCategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaxonomyServiceTest {

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SubCategoryRepository subCategoryRepository;

    @Mock
    private ConfigEventProducer eventProducer;

    @InjectMocks
    private TaxonomyService taxonomyService;

    @Test
    @DisplayName("taxonomy 조회는 useYn=Y인 지역/카테고리/서브카테고리만 반환한다")
    void getTaxonomy_filtersOnlyActiveItems() {
        Region activeRegion = Region.builder().regionCode("CHEONGJU").regionName("청주시").sortOrder(1).build();
        Category activeCategory = Category.builder().categoryCode("FREE").categoryName("자유").sortOrder(1).build();
        SubCategory activeSub = SubCategory.builder()
                .categoryCode("FREE")
                .subCategoryCode("NOTICE")
                .subCategoryName("공지")
                .sortOrder(1)
                .build();

        given(regionRepository.findAllByUseYnOrderBySortOrderAsc("Y")).willReturn(List.of(activeRegion));
        given(categoryRepository.findAllByUseYnOrderBySortOrderAsc("Y")).willReturn(List.of(activeCategory));
        given(subCategoryRepository.findAllByCategoryCodeAndUseYnOrderBySortOrderAsc("FREE", "Y"))
                .willReturn(List.of(activeSub));

        TaxonomyResponse result = taxonomyService.getTaxonomy();

        assertThat(result.regions()).hasSize(1);
        assertThat(result.regions().getFirst().regionCode()).isEqualTo("CHEONGJU");
        assertThat(result.categories()).hasSize(1);
        assertThat(result.categories().getFirst().categoryCode()).isEqualTo("FREE");
        assertThat(result.categories().getFirst().subCategories()).hasSize(1);
        assertThat(result.categories().getFirst().subCategories().getFirst().subCategoryCode()).isEqualTo("NOTICE");
    }

    @Test
    @DisplayName("지역 비활성화 성공 시 useYn이 N으로 변경되고 이벤트가 발행된다")
    void deactivateRegion_success() {
        Region region = Region.builder().regionCode("CHEONGJU").regionName("청주시").sortOrder(1).build();
        given(regionRepository.findByRegionCode("CHEONGJU")).willReturn(Optional.of(region));

        taxonomyService.deactivateRegion("CHEONGJU");

        assertThat(region.getUseYn()).isEqualTo("N");
        verify(eventProducer).publishTaxonomyChanged("REGION_DEACTIVATED", "CHEONGJU");
    }

    @Test
    @DisplayName("이미 비활성화된 지역을 비활성화하면 409 예외를 던진다")
    void deactivateRegion_alreadyInactive() {
        Region inactiveRegion = Region.builder().regionCode("CHEONGJU").regionName("청주시").sortOrder(1).build();
        inactiveRegion.deactivate();
        given(regionRepository.findByRegionCode("CHEONGJU")).willReturn(Optional.of(inactiveRegion));

        assertThatThrownBy(() -> taxonomyService.deactivateRegion("CHEONGJU"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.REGION_ALREADY_INACTIVE));

        verify(eventProducer, never()).publishTaxonomyChanged(anyString(), anyString());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 비활성화 요청은 404 예외를 던진다")
    void deactivateCategory_notFound() {
        given(categoryRepository.findByCategoryCode("FREE")).willReturn(Optional.empty());

        assertThatThrownBy(() -> taxonomyService.deactivateCategory("FREE"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND));

        verify(eventProducer, never()).publishTaxonomyChanged(anyString(), anyString());
    }
}

