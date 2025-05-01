package com.plamason.postmanager.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface InstagramApiService {
    String test();
    Object validateToken(String token);
    Object createPost(String userId, String accessToken, String imageUrl, String content);
    Object createCarouselPost(String userId, String accessToken, List<String> imageUrlList, String content);
}