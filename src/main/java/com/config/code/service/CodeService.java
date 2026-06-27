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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeService {

    private static final String ACTIVE = "Y";

    private final CodeGroupRepository codeGroupRepository;
    private final CodeItemRepository codeItemRepository;
    private final ConfigEventProducer eventProducer;

    // ─── 코드 그룹 목록 조회 [ADMIN] ─────────────────────────────────────────

    public List<CodeGroupResponse> getCodeGroups() {
        return codeGroupRepository.findAllByUseYnOrderByGroupCodeAsc(ACTIVE)
                .stream()
                .map(CodeGroupResponse::from)
                .toList();
    }

    // ─── 코드 항목 조회 (useYn=Y만 반환) ─────────────────────────────────────

    @Cacheable(value = "codeItems", key = "#groupCode")
    public List<CodeItemResponse> getCodeItems(String groupCode) {
        CodeGroup group = codeGroupRepository.findByGroupCode(groupCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.CODE_GROUP_NOT_FOUND));
        return codeItemRepository.findAllByGroupIdAndUseYnOrderBySortOrderAsc(group.getId(), ACTIVE)
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

    // ─── 코드 그룹 수정·비활성화 [ADMIN] ─────────────────────────────────────

    @Transactional
    public void updateCodeGroup(String groupCode, UpdateCodeGroupRequest request) {
        CodeGroup group = codeGroupRepository.findByGroupCode(groupCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.CODE_GROUP_NOT_FOUND));

        group.update(request.groupName(), request.description());

        if ("N".equals(request.useYn())) {
            if (!group.isActive()) {
                throw new BusinessException(ErrorCode.CODE_GROUP_ALREADY_INACTIVE);
            }
            group.deactivate();
        } else {
            group.activate();
        }

        eventProducer.publishTaxonomyChanged("CODE_GROUP_UPDATED", groupCode);
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
    @CacheEvict(value = "codeItems", allEntries = true)
    public CodeItemResponse updateCodeItem(Long id, UpdateCodeItemRequest request) {
        validateUseYn(request.useYn());

        CodeItem item = codeItemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CODE_ITEM_NOT_FOUND));

        applyItemUpdate(item, request);
        codeItemRepository.flush();

        eventProducer.publishTaxonomyChanged("CODE_ITEM_UPDATED", String.valueOf(id));
        return CodeItemResponse.from(item);
    }

    @Transactional
    @CacheEvict(value = "codeItems", allEntries = true)
    public CodeItemResponse updateCodeItem(String groupCode, String code, UpdateCodeItemRequest request) {
        validateUseYn(request.useYn());

        CodeGroup group = codeGroupRepository.findByGroupCode(groupCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.CODE_GROUP_NOT_FOUND));

        CodeItem item = codeItemRepository.findByGroupIdAndCode(group.getId(), code)
                .orElseThrow(() -> new BusinessException(ErrorCode.CODE_ITEM_NOT_FOUND));

        applyItemUpdate(item, request);
        codeItemRepository.flush();

        eventProducer.publishTaxonomyChanged("CODE_ITEM_UPDATED", groupCode);
        return CodeItemResponse.from(item);
    }

    @Transactional
    @CacheEvict(value = "codeItems", allEntries = true)
    public List<CodeItemResponse> updateCodeItemSortOrders(String groupCode, List<UpdateCodeItemSortRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        CodeGroup group = codeGroupRepository.findByGroupCode(groupCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.CODE_GROUP_NOT_FOUND));

        List<CodeItem> items = codeItemRepository.findAllByGroupIdOrderBySortOrderAsc(group.getId());
        Set<String> requestCodes = new HashSet<>();

        for (UpdateCodeItemSortRequest request : requests) {
            if (request.code() == null || request.code().isBlank() || !requestCodes.add(request.code())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }

            CodeItem item = items.stream()
                    .filter(candidate -> request.code().equals(candidate.getCode()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.CODE_ITEM_NOT_FOUND));

            item.changeSortOrder(request.sortOrder());
        }

        codeItemRepository.flush();

        eventProducer.publishTaxonomyChanged("CODE_ITEM_UPDATED", groupCode);
        return items.stream()
                .sorted((left, right) -> Integer.compare(left.getSortOrder(), right.getSortOrder()))
                .map(CodeItemResponse::from)
                .toList();
    }

    private void applyItemUpdate(CodeItem item, UpdateCodeItemRequest request) {
        item.update(request.name(), request.sortOrder());

        if ("N".equals(request.useYn())) {
            item.deactivate();
            return;
        }

        item.activate();
    }

    private void validateUseYn(String useYn) {
        if (!"Y".equals(useYn) && !"N".equals(useYn)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
