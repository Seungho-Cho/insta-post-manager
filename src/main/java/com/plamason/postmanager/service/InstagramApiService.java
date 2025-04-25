package com.plamason.postmanager.service;

import org.springframework.stereotype.Service;

@Service
public class InstagramApiService {
    public String test() {
        return "test";
    }

    public Object validateToken(String token) {
        // TODO: Instagram API를 통해 토큰 유효성 검증
        return null;
    }

    public Object createPost(String token, String content, String imageUrl) {
        // TODO: Instagram API를 통해 게시물 생성
        return null;
    }
} 