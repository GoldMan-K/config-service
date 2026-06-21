package com.config.taxonomy.repository;

import com.config.taxonomy.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByCategoryCode(String categoryCode);

    Optional<Category> findByCategoryCode(String categoryCode);

    List<Category> findAllByUseYnOrderBySortOrderAsc(String useYn);
}
