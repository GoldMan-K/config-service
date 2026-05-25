package com.config.code.service;

import com.config.code.domain.CodeGroup;
import com.config.code.domain.CodeItem;
import com.config.code.dto.*;
import com.config.code.repository.CodeGroupRepository;
import com.config.code.repository.CodeItemRepository;
import com.config.global.exception.BusinessException;
import com.config.global.exception.ErrorCode;
import com.config.kafka.producer.ConfigEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeService {

    private final CodeGroupRepository codeGroupRepository;
    private final CodeItemRepository codeItemRepository;
    private final ConfigEventProducer eventProducer;

    // ─── 코드 항목 조회 (CDN 캐싱 권장) ─────────────────────────────────────

    @Cacheable(value = "codeItems", key = "#groupCode")
    public List<CodeItemResponse> getCodeItems(String groupCode) {
        CodeGroup group = codeGroupRepository.findByGroupCode(groupCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.CODE_GROUP_NOT_FOUND));
        return codeItemRepository.findAllByGroupIdOrderBySortOrderAsc(group.getId())
                .stream()
                .map(CodeItemResponse::from)
                .toList();
    }

    // ─── 코드 그룹 생성 [ADMIN] ──────────────────────────────────────────────

    @Transactional
    public void createCodeGroup(CreateCodeGroupRequest request) {
        if (codeGroupRepository.existsByGroupCode(request.groupCode())) {
            throw new BusinessException(ErrorCode.CODE_GROUP_ALREADY_EXISTS);
        }
        codeGroupRepository.save(CodeGroup.builder()
                .groupCode(request.groupCode())
                .groupName(request.groupName())
                .description(request.description())
                .build());
    }

    // ─── 코드 항목 추가 [ADMIN] ──────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = "codeItems", key = "#request.groupCode()")
    public void createCodeItem(CreateCodeItemRequest request) {
        CodeGroup group = codeGroupRepository.findByGroupCode(request.groupCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.CODE_GROUP_NOT_FOUND));

        if (codeItemRepository.existsByGroupIdAndCode(group.getId(), request.code())) {
            throw new BusinessException(ErrorCode.CODE_ITEM_ALREADY_EXISTS);
        }
        codeItemRepository.save(CodeItem.builder()
                .groupId(group.getId())
                .code(request.code())
                .name(request.name())
                .sortOrder(request.sortOrder())
                .build());

        eventProducer.publishTaxonomyChanged("CODE_ITEM_ADDED", request.groupCode());
    }

    // ─── 코드 항목 수정·비활성화 [ADMIN] ─────────────────────────────────────

    @Transactional
    public void updateCodeItem(Long id, UpdateCodeItemRequest request) {
        CodeItem item = codeItemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CODE_ITEM_NOT_FOUND));
        item.update(request.name(), request.sortOrder());
        if ("N".equals(request.useYn())) item.deactivate();
        else item.activate();

        eventProducer.publishTaxonomyChanged("CODE_ITEM_UPDATED", String.valueOf(id));
    }
}
