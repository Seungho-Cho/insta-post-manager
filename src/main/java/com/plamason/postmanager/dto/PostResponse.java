package com.plamason.postmanager.dto;

import com.plamason.postmanager.entity.PostStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private List<String> imageUrls;
    private List<String> tags;
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime postedAt;
    private PostStatus status;
    private String instagramPostId;
    private String errorMessage;
} 