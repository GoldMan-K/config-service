package com.config.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // 코드 그룹
    CODE_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "코드 그룹을 찾을 수 없습니다."),
    CODE_GROUP_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 코드 그룹입니다."),

    // 코드 항목
    CODE_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "코드 항목을 찾을 수 없습니다."),
    CODE_ITEM_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 코드 항목입니다."),

    // 지역
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "지역을 찾을 수 없습니다."),
    REGION_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 지역 코드입니다."),
    REGION_ALREADY_INACTIVE(HttpStatus.CONFLICT, "이미 비활성화된 지역입니다."),

    // 카테고리
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),
    CATEGORY_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 카테고리 코드입니다."),
    CATEGORY_ALREADY_INACTIVE(HttpStatus.CONFLICT, "이미 비활성화된 카테고리입니다."),

    // 서브카테고리
    SUB_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "서브카테고리를 찾을 수 없습니다."),
    SUB_CATEGORY_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 서브카테고리 코드입니다.");

    private final HttpStatus status;
    private final String message;
}
