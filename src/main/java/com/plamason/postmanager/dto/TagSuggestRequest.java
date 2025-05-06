package com.plamason.postmanager.dto;

import lombok.Data;

@Data
public class TagSuggestRequest {
    private String content;
    private String title;
    private String description;
}