package com.config.taxonomy.controller;

import com.config.global.exception.BusinessException;
import com.config.global.exception.ErrorCode;
import com.config.taxonomy.dto.*;
import com.config.taxonomy.service.TaxonomyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Taxonomy", description = "지역·카테고리·서브카테고리 API")
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class TaxonomyController {

    private final TaxonomyService taxonomyService;

    @Operation(
            summary = "전체 Taxonomy 트리 조회",
            description = "지역 + 카테고리 + 서브카테고리 전체 계층을 반환합니다. CDN 캐싱 적극 활용 권장."
    )
    @GetMapping("/taxonomy")
    public ResponseEntity<TaxonomyResponse> getTaxonomy() {
        return ResponseEntity.ok(taxonomyService.getTaxonomy());
    }

    @Operation(summary = "[ADMIN] 지역 추가")
    @PostMapping("/regions")
    public ResponseEntity<Void> createRegion(
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody CreateRegionRequest request
    ) {
        validateMemberId(memberId);
        taxonomyService.createRegion(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "[ADMIN] 카테고리 추가")
    @PostMapping("/categories")
    public ResponseEntity<Void> createCategory(
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody CreateCategoryRequest request
    ) {
        validateMemberId(memberId);
        taxonomyService.createCategory(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "[ADMIN] 서브카테고리 추가")
    @PostMapping("/sub-categories")
    public ResponseEntity<Void> createSubCategory(
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody CreateSubCategoryRequest request
    ) {
        validateMemberId(memberId);
        taxonomyService.createSubCategory(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "[ADMIN] 지역 비활성화", description = "물리 삭제 대신 useYn=N으로 소프트삭제 처리합니다.")
    @PatchMapping("/regions/{regionCode}")
    public ResponseEntity<Void> deactivateRegion(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable String regionCode
    ) {
        validateMemberId(memberId);
        taxonomyService.deactivateRegion(regionCode);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "[ADMIN] 카테고리 비활성화", description = "물리 삭제 대신 useYn=N으로 소프트삭제 처리합니다.")
    @PatchMapping("/categories/{categoryCode}")
    public ResponseEntity<Void> deactivateCategory(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable String categoryCode
    ) {
        validateMemberId(memberId);
        taxonomyService.deactivateCategory(categoryCode);
        return ResponseEntity.noContent().build();
    }

    private void validateMemberId(Long memberId) {
        if (memberId == null || memberId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
