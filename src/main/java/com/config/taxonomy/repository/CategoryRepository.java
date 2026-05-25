package com.config.taxonomy.repository;

import com.config.taxonomy.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByCategoryCode(String categoryCode);

    List<Category> findAllByOrderBySortOrderAsc();
}
