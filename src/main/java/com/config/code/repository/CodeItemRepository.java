package com.config.code.repository;

import com.config.code.domain.CodeItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CodeItemRepository extends JpaRepository<CodeItem, Long> {

    boolean existsByGroupIdAndCode(Long groupId, String code);

    Optional<CodeItem> findByGroupIdAndCode(Long groupId, String code);

    List<CodeItem> findAllByGroupIdOrderBySortOrderAsc(Long groupId);

    List<CodeItem> findAllByGroupIdAndUseYnOrderBySortOrderAsc(Long groupId, String useYn);
}
