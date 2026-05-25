package com.config.taxonomy.controller;

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
    public ResponseEntity<Void> createRegion(@RequestBody CreateRegionRequest request) {
        taxonomyService.createRegion(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "[ADMIN] 카테고리 추가")
    @PostMapping("/categories")
    public ResponseEntity<Void> createCategory(@RequestBody CreateCategoryRequest request) {
        taxonomyService.createCategory(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "[ADMIN] 서브카테고리 추가")
    @PostMapping("/sub-categories")
    public ResponseEntity<Void> createSubCategory(@RequestBody CreateSubCategoryRequest request) {
        taxonomyService.createSubCategory(request);
        return ResponseEntity.ok().build();
    }
}
