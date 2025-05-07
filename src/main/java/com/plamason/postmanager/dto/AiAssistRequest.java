package com.plamason.postmanager.dto;

import lombok.Data;

@Data
public class AiAssistRequest {
    private String content;
    private String title;
    private String description;
}