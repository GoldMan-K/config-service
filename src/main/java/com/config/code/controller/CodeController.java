package com.config.code.controller;

import com.config.code.dto.*;
import com.config.code.service.CodeService;
import com.config.global.exception.BusinessException;
import com.config.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Code", description = "공통 코드 API")
@RestController
@RequestMapping("/api/config/codes")
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;

    @Operation(summary = "[ADMIN] 코드 그룹 목록 조회", description = "useYn=Y인 그룹만 반환합니다.")
    @GetMapping("/groups")
    public ResponseEntity<List<CodeGroupResponse>> getCodeGroups(
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        validateMemberId(memberId);
        return ResponseEntity.ok(codeService.getCodeGroups());
    }

    @Operation(summary = "코드 그룹별 항목 조회", description = "useYn=Y 항목만 반환하며, 각 항목에는 수정용 id가 항상 포함됩니다.")
    @GetMapping("/{groupCode}")
    public ResponseEntity<List<CodeItemResponse>> getCodeItems(@PathVariable String groupCode) {
        return ResponseEntity.ok(codeService.getCodeItems(groupCode));
    }

    @Operation(summary = "[ADMIN] 코드 그룹 생성")
    @PostMapping("/groups")
    public ResponseEntity<Void> createCodeGroup(
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody CreateCodeGroupRequest request
    ) {
        validateMemberId(memberId);
        codeService.createCodeGroup(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "[ADMIN] 코드 그룹 수정·비활성화",
            description = "groupName·description 수정 가능. useYn=N 전송 시 소프트삭제(비활성화) 처리됩니다."
    )
    @PatchMapping("/groups/{groupCode}")
    public ResponseEntity<Void> updateCodeGroup(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable String groupCode,
            @RequestBody UpdateCodeGroupRequest request
    ) {
        validateMemberId(memberId);
        codeService.updateCodeGroup(groupCode, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "[ADMIN] 코드 항목 추가")
    @PostMapping("/items")
    public ResponseEntity<Void> createCodeItem(
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody CreateCodeItemRequest request
    ) {
        validateMemberId(memberId);
        codeService.createCodeItem(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "[ADMIN] 코드 항목 수정·비활성화",
            description = "물리 삭제는 지원하지 않으며 useYn=N으로 소프트삭제(비활성화) 처리합니다. 수정 후 최신 항목 전체를 반환합니다."
    )
    @PatchMapping("/items/{id}")
    public ResponseEntity<CodeItemResponse> updateCodeItem(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long id,
            @RequestBody UpdateCodeItemRequest request
    ) {
        validateMemberId(memberId);
        return ResponseEntity.ok(codeService.updateCodeItem(id, request));
    }

    @Operation(
            summary = "[ADMIN] 코드 항목 수정·비활성화(groupCode + code)",
            description = "id 기반 수정 API의 대체 키 버전입니다. 물리 삭제는 지원하지 않으며 useYn=N으로 소프트삭제 처리합니다."
    )
    @PatchMapping("/{groupCode}/items/{code}")
    public ResponseEntity<CodeItemResponse> updateCodeItemByCode(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable String groupCode,
            @PathVariable String code,
            @RequestBody UpdateCodeItemRequest request
    ) {
        validateMemberId(memberId);
        return ResponseEntity.ok(codeService.updateCodeItem(groupCode, code, request));
    }

    @Operation(
            summary = "[ADMIN] 코드 항목 정렬 일괄 저장",
            description = "운영 화면에서 여러 코드 항목의 sortOrder를 한 번에 저장합니다. payload는 code + sortOrder 배열입니다."
    )
    @PatchMapping("/{groupCode}/items/sort")
    public ResponseEntity<List<CodeItemResponse>> updateCodeItemSortOrders(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable String groupCode,
            @RequestBody List<UpdateCodeItemSortRequest> requests
    ) {
        validateMemberId(memberId);
        return ResponseEntity.ok(codeService.updateCodeItemSortOrders(groupCode, requests));
    }

    private void validateMemberId(Long memberId) {
        if (memberId == null || memberId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
