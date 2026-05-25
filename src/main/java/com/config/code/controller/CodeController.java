package com.config.code.controller;

import com.config.code.dto.*;
import com.config.code.service.CodeService;
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

    @Operation(summary = "코드 그룹별 항목 조회", description = "클라이언트 캐싱 권장. use_yn=Y 항목 포함.")
    @GetMapping("/{groupCode}")
    public ResponseEntity<List<CodeItemResponse>> getCodeItems(@PathVariable String groupCode) {
        return ResponseEntity.ok(codeService.getCodeItems(groupCode));
    }

    @Operation(summary = "[ADMIN] 코드 그룹 생성")
    @PostMapping("/groups")
    public ResponseEntity<Void> createCodeGroup(@RequestBody CreateCodeGroupRequest request) {
        codeService.createCodeGroup(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "[ADMIN] 코드 항목 추가")
    @PostMapping("/items")
    public ResponseEntity<Void> createCodeItem(@RequestBody CreateCodeItemRequest request) {
        codeService.createCodeItem(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "[ADMIN] 코드 항목 수정·비활성화", description = "useYn=N으로 설정 시 비활성화. 기존 데이터는 보존됩니다.")
    @PatchMapping("/items/{id}")
    public ResponseEntity<Void> updateCodeItem(
            @PathVariable Long id,
            @RequestBody UpdateCodeItemRequest request
    ) {
        codeService.updateCodeItem(id, request);
        return ResponseEntity.ok().build();
    }
}
