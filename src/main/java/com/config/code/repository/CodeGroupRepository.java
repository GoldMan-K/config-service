package com.config.code.repository;

import com.config.code.domain.CodeGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodeGroupRepository extends JpaRepository<CodeGroup, Long> {

    boolean existsByGroupCode(String groupCode);

    Optional<CodeGroup> findByGroupCode(String groupCode);
}
