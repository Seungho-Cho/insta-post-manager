package com.plamason.postmanager.entity;

public enum PostStatus {
    DRAFT("임시저장"),
    SCHEDULED("예약됨"),
    PUBLISHED("게시됨"),
    FAILED("실패"),
    DELETED("삭제됨");

    private final String description;

    PostStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 