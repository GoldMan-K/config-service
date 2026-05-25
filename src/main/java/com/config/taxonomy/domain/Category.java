package com.config.taxonomy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "config_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String categoryCode;
    private String categoryName;
    private int sortOrder;
    private String useYn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public Category(String categoryCode, String categoryName, int sortOrder) {
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
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
