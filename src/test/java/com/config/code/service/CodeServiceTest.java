package com.config.code.service;

import com.config.code.domain.CodeGroup;
import com.config.code.domain.CodeItem;
import com.config.code.dto.CodeGroupResponse;
import com.config.code.dto.CodeItemResponse;
import com.config.code.dto.UpdateCodeItemRequest;
import com.config.code.dto.UpdateCodeItemSortRequest;
import com.config.code.dto.UpdateCodeGroupRequest;
import com.config.code.repository.CodeGroupRepository;
import com.config.code.repository.CodeItemRepository;
import com.config.global.exception.BusinessException;
import com.config.global.exception.ErrorCode;
import com.config.kafka.producer.ConfigEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CodeServiceTest {

    @Mock CodeGroupRepository codeGroupRepository;
    @Mock CodeItemRepository codeItemRepository;
    @Mock ConfigEventProducer eventProducer;

    @InjectMocks CodeService codeService;

    @Test
    @DisplayName("코드 그룹 목록은 useYn=Y인 그룹만 반환한다")
    void getCodeGroups_returnsOnlyActiveGroups() {
        CodeGroup group = CodeGroup.builder()
                .groupCode("POST_STATUS").groupName("게시글 상태").description("desc").build();
        given(codeGroupRepository.findAllByUseYnOrderByGroupCodeAsc("Y")).willReturn(List.of(group));

        List<CodeGroupResponse> result = codeService.getCodeGroups();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().groupCode()).isEqualTo("POST_STATUS");
        assertThat(result.getFirst().useYn()).isEqualTo("Y");
    }

    @Test
    @DisplayName("코드 항목 조회는 useYn=Y인 항목만 반환한다")
    void getCodeItems_returnsOnlyActiveItems() {
        CodeGroup group = CodeGroup.builder().groupCode("POST_STATUS").groupName("게시글상태").description("").build();
        CodeItem item = CodeItem.builder().groupId(1L).code("NORMAL").name("정상").sortOrder(1).build();
        ReflectionTestUtils.setField(group, "id", 1L);
        ReflectionTestUtils.setField(item, "id", 10L);

        given(codeGroupRepository.findByGroupCode("POST_STATUS")).willReturn(Optional.of(group));
        given(codeItemRepository.findAllByGroupIdAndUseYnOrderBySortOrderAsc(group.getId(), "Y"))
                .willReturn(List.of(item));

        var result = codeService.getCodeItems("POST_STATUS");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(10L);
        assertThat(result.getFirst().code()).isEqualTo("NORMAL");
    }

    @Test
    @DisplayName("코드 항목 id 기반 수정은 최신 항목 전체를 반환한다")
    void updateCodeItem_byId_returnsUpdatedItem() {
        CodeItem item = CodeItem.builder().groupId(1L).code("NORMAL").name("정상").sortOrder(1).build();
        ReflectionTestUtils.setField(item, "id", 10L);

        given(codeItemRepository.findById(10L)).willReturn(Optional.of(item));

        CodeItemResponse result = codeService.updateCodeItem(10L,
                new UpdateCodeItemRequest("숨김", 3, "N"));

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.code()).isEqualTo("NORMAL");
        assertThat(result.name()).isEqualTo("숨김");
        assertThat(result.sortOrder()).isEqualTo(3);
        assertThat(result.useYn()).isEqualTo("N");
        assertThat(result.updatedAt()).isNotNull();
        verify(codeItemRepository).flush();
        verify(eventProducer).publishTaxonomyChanged("CODE_ITEM_UPDATED", "10");
    }

    @Test
    @DisplayName("코드 항목 groupCode+code 기반 수정은 최신 항목 전체를 반환한다")
    void updateCodeItem_byGroupCodeAndCode_returnsUpdatedItem() {
        CodeGroup group = CodeGroup.builder().groupCode("POST_STATUS").groupName("게시글상태").description("").build();
        CodeItem item = CodeItem.builder().groupId(1L).code("NORMAL").name("정상").sortOrder(1).build();
        ReflectionTestUtils.setField(group, "id", 1L);
        ReflectionTestUtils.setField(item, "id", 10L);

        given(codeGroupRepository.findByGroupCode("POST_STATUS")).willReturn(Optional.of(group));
        given(codeItemRepository.findByGroupIdAndCode(1L, "NORMAL")).willReturn(Optional.of(item));

        CodeItemResponse result = codeService.updateCodeItem("POST_STATUS", "NORMAL",
                new UpdateCodeItemRequest("운영중", 5, "Y"));

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.code()).isEqualTo("NORMAL");
        assertThat(result.name()).isEqualTo("운영중");
        assertThat(result.sortOrder()).isEqualTo(5);
        assertThat(result.useYn()).isEqualTo("Y");
        assertThat(result.updatedAt()).isNotNull();
        verify(codeItemRepository).flush();
        verify(eventProducer).publishTaxonomyChanged("CODE_ITEM_UPDATED", "POST_STATUS");
    }

    @Test
    @DisplayName("코드 항목 정렬 일괄 저장은 여러 항목의 sortOrder를 변경한다")
    void updateCodeItemSortOrders_updatesMultipleItems() {
        CodeGroup group = CodeGroup.builder().groupCode("POST_STATUS").groupName("게시글상태").description("").build();
        CodeItem first = CodeItem.builder().groupId(1L).code("NORMAL").name("정상").sortOrder(1).build();
        CodeItem second = CodeItem.builder().groupId(1L).code("HIDDEN").name("숨김").sortOrder(2).build();
        ReflectionTestUtils.setField(group, "id", 1L);

        given(codeGroupRepository.findByGroupCode("POST_STATUS")).willReturn(Optional.of(group));
        given(codeItemRepository.findAllByGroupIdOrderBySortOrderAsc(1L)).willReturn(List.of(first, second));

        List<CodeItemResponse> result = codeService.updateCodeItemSortOrders(
                "POST_STATUS",
                List.of(
                        new UpdateCodeItemSortRequest("NORMAL", 2),
                        new UpdateCodeItemSortRequest("HIDDEN", 1)
                )
        );

        assertThat(first.getSortOrder()).isEqualTo(2);
        assertThat(second.getSortOrder()).isEqualTo(1);
        assertThat(result).extracting(CodeItemResponse::code).containsExactly("HIDDEN", "NORMAL");
        verify(codeItemRepository).flush();
        verify(eventProducer).publishTaxonomyChanged("CODE_ITEM_UPDATED", "POST_STATUS");
    }

    @Test
    @DisplayName("코드 그룹 수정 성공 시 이벤트가 발행된다")
    void updateCodeGroup_success() {
        CodeGroup group = CodeGroup.builder()
                .groupCode("POST_STATUS").groupName("게시글 상태").description("old desc").build();
        given(codeGroupRepository.findByGroupCode("POST_STATUS")).willReturn(Optional.of(group));

        codeService.updateCodeGroup("POST_STATUS",
                new UpdateCodeGroupRequest("새 그룹명", "새 설명", "Y"));

        assertThat(group.getGroupName()).isEqualTo("새 그룹명");
        assertThat(group.getDescription()).isEqualTo("새 설명");
        assertThat(group.getUseYn()).isEqualTo("Y");
        verify(eventProducer).publishTaxonomyChanged("CODE_GROUP_UPDATED", "POST_STATUS");
    }

    @Test
    @DisplayName("코드 그룹 비활성화(useYn=N) 성공 시 useYn이 N으로 변경된다")
    void updateCodeGroup_deactivate() {
        CodeGroup group = CodeGroup.builder()
                .groupCode("POST_STATUS").groupName("게시글 상태").description("desc").build();
        given(codeGroupRepository.findByGroupCode("POST_STATUS")).willReturn(Optional.of(group));

        codeService.updateCodeGroup("POST_STATUS",
                new UpdateCodeGroupRequest("게시글 상태", "desc", "N"));

        assertThat(group.getUseYn()).isEqualTo("N");
        verify(eventProducer).publishTaxonomyChanged("CODE_GROUP_UPDATED", "POST_STATUS");
    }

    @Test
    @DisplayName("이미 비활성화된 그룹을 비활성화하면 409 예외를 던진다")
    void updateCodeGroup_alreadyInactive_throws409() {
        CodeGroup group = CodeGroup.builder()
                .groupCode("POST_STATUS").groupName("게시글 상태").description("desc").build();
        group.deactivate();
        given(codeGroupRepository.findByGroupCode("POST_STATUS")).willReturn(Optional.of(group));

        assertThatThrownBy(() -> codeService.updateCodeGroup("POST_STATUS",
                new UpdateCodeGroupRequest("게시글 상태", "desc", "N")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.CODE_GROUP_ALREADY_INACTIVE));

        verify(eventProducer, never()).publishTaxonomyChanged(anyString(), anyString());
    }

    @Test
    @DisplayName("존재하지 않는 그룹 코드 수정 요청은 404 예외를 던진다")
    void updateCodeGroup_notFound_throws404() {
        given(codeGroupRepository.findByGroupCode("NONEXISTENT")).willReturn(Optional.empty());

        assertThatThrownBy(() -> codeService.updateCodeGroup("NONEXISTENT",
                new UpdateCodeGroupRequest("이름", "설명", "Y")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.CODE_GROUP_NOT_FOUND));
    }
}

