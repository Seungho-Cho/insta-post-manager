package com.plamason.postmanager.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface InstagramApiService {
    String test();
    String validateToken(String token);
    String createPost(String imageUrl, String content);
    String createCarouselPost(List<String> imageUrlList, String content);
}