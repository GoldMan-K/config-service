package com.config.code.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "config_code_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodeGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String groupCode;
    private String groupName;
    private String description;
    private String useYn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public CodeGroup(String groupCode, String groupName, String description) {
        this.groupCode = groupCode;
        this.groupName = groupName;
        this.description = description;
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

    public void update(String groupName, String description) {
        this.groupName = groupName;
        this.description = description;
    }

    public void deactivate() { this.useYn = "N"; }
    public void activate()   { this.useYn = "Y"; }
    public boolean isActive(){ return "Y".equals(this.useYn); }
}
