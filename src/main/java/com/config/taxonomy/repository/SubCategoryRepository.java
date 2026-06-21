package com.config.taxonomy.repository;

import com.config.taxonomy.domain.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {

    boolean existsByCategoryCodeAndSubCategoryCode(String categoryCode, String subCategoryCode);

    List<SubCategory> findAllByCategoryCodeAndUseYnOrderBySortOrderAsc(String categoryCode, String useYn);
}
