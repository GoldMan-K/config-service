CREATE TABLE IF NOT EXISTS config_code_group (
    id           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '코드 그룹 PK',
    group_code   VARCHAR(50)   NOT NULL                 COMMENT '그룹 코드',
    group_name   VARCHAR(100)  NOT NULL                 COMMENT '그룹 명칭',
    description  VARCHAR(255)  NULL                     COMMENT '설명',
    use_yn       CHAR(1)       NOT NULL DEFAULT 'Y',
    created_at   DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at   DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_group_code (group_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='공통 코드 그룹';

CREATE TABLE IF NOT EXISTS config_code_item (
    id          BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '코드 항목 PK',
    group_id    BIGINT        NOT NULL                 COMMENT '코드 그룹 FK',
    code        VARCHAR(50)   NOT NULL                 COMMENT '코드 값',
    name        VARCHAR(100)  NOT NULL                 COMMENT '코드 명칭',
    sort_order  INT           NOT NULL DEFAULT 0,
    use_yn      CHAR(1)       NOT NULL DEFAULT 'Y',
    created_at  DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_group_code (group_id, code),
    KEY idx_item_group (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='공통 코드 항목';

CREATE TABLE IF NOT EXISTS config_region (
    id           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '지역 PK',
    region_code  VARCHAR(30)   NOT NULL                 COMMENT '지역 코드',
    region_name  VARCHAR(50)   NOT NULL                 COMMENT '지역 명칭',
    sort_order   INT           NOT NULL DEFAULT 0,
    use_yn       CHAR(1)       NOT NULL DEFAULT 'Y',
    created_at   DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at   DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_region_code (region_code),
    UNIQUE KEY uq_region_name (region_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='지역';

CREATE TABLE IF NOT EXISTS config_category (
    id             BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '카테고리 PK',
    category_code  VARCHAR(30)   NOT NULL                 COMMENT '카테고리 코드',
    category_name  VARCHAR(50)   NOT NULL                 COMMENT '카테고리 명칭',
    sort_order     INT           NOT NULL DEFAULT 0,
    use_yn         CHAR(1)       NOT NULL DEFAULT 'Y',
    created_at     DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at     DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_category_code (category_code),
    UNIQUE KEY uq_category_name (category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='카테고리';

CREATE TABLE IF NOT EXISTS config_sub_category (
    id                  BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '서브카테고리 PK',
    category_code       VARCHAR(30)   NOT NULL                 COMMENT '상위 카테고리 코드',
    sub_category_code   VARCHAR(30)   NOT NULL                 COMMENT '서브카테고리 코드',
    sub_category_name   VARCHAR(50)   NOT NULL                 COMMENT '서브카테고리 명칭',
    sort_order          INT           NOT NULL DEFAULT 0,
    use_yn              CHAR(1)       NOT NULL DEFAULT 'Y',
    created_at          DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at          DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_sub_code (category_code, sub_category_code),
    KEY idx_sub_category (category_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='서브카테고리';

-- 충북 지역 기본 데이터
INSERT INTO config_region (region_code, region_name, sort_order) VALUES
('CHUNGBUK',      '충청북도',   1),
('CHEONGJU',      '청주시',     2),
('CHUNGJU',       '충주시',     3),
('JECHEON',       '제천시',     4),
('BOEUN',         '보은군',     5),
('OKCHEON',       '옥천군',     6),
('YEONGDONG',     '영동군',     7),
('JEUNGPYEONG',   '증평군',     8),
('JINCHEON',      '진천군',     9),
('GOESAN',        '괴산군',    10),
('EUMSEONG',      '음성군',    11),
('DANYANG',       '단양군',    12);
