package com.config.taxonomy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "config_sub_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String categoryCode;   // 상위 카테고리 코드
    private String subCategoryCode;
    private String subCategoryName;
    private int sortOrder;
    private String useYn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public SubCategory(String categoryCode, String subCategoryCode,
                       String subCategoryName, int sortOrder) {
        this.categoryCode = categoryCode;
        this.subCategoryCode = subCategoryCode;
        this.subCategoryName = subCategoryName;
        this.sortOrder = sortOrder;
        this.useYn = "Y";
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() { this.useYn = "N"; }
    public boolean isActive(){ return "Y".equals(this.useYn); }
}
