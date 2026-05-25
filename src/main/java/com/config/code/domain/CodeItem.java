package com.config.code.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "config_code_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long groupId;
    private String code;
    private String name;
    private int sortOrder;
    private String useYn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public CodeItem(Long groupId, String code, String name, int sortOrder) {
        this.groupId = groupId;
        this.code = code;
        this.name = name;
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

    public void update(String name, int sortOrder) {
        this.name = name;
        this.sortOrder = sortOrder;
    }

    public void deactivate() { this.useYn = "N"; }
    public void activate()   { this.useYn = "Y"; }
    public boolean isActive(){ return "Y".equals(this.useYn); }
}
