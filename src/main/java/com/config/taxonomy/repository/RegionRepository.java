package com.config.taxonomy.repository;

import com.config.taxonomy.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegionRepository extends JpaRepository<Region, Long> {

    boolean existsByRegionCode(String regionCode);

    List<Region> findAllByOrderBySortOrderAsc();
}
