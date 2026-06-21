package com.config.taxonomy.repository;

import com.config.taxonomy.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    boolean existsByRegionCode(String regionCode);

    Optional<Region> findByRegionCode(String regionCode);

    List<Region> findAllByUseYnOrderBySortOrderAsc(String useYn);
}
