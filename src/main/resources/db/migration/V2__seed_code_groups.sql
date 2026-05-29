-- 초기 공통 코드 그룹 시드
-- POST_STATUS, USER_LEVEL, REPORT_REASON 404 방지를 위한 최소 데이터

INSERT IGNORE INTO config_code_group (
    group_code,
    group_name,
    description,
    use_yn,
    created_at,
    updated_at
) VALUES
    ('POST_STATUS', '게시글 상태', '게시글 상태 관련 코드 그룹', 'Y', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
    ('USER_LEVEL', '사용자 등급', '사용자 등급 관련 코드 그룹', 'Y', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
    ('REPORT_REASON', '신고 사유', '신고 사유 관련 코드 그룹', 'Y', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3));

