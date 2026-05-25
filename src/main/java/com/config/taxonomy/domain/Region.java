package com.config.taxonomy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "config_region")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String regionCode;
    private String regionName;
    private int sortOrder;
    private String useYn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public Region(String regionCode, String regionName, int sortOrder) {
        this.regionCode = regionCode;
        this.regionName = regionName;
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
