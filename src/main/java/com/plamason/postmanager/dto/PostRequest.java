package com.plamason.postmanager.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PostRequest {
    private String title;
    private String content;
    private List<String> imageUrls;
    private List<String> tags;
    private String author;
    private String status;
} 