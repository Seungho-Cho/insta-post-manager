package com.plamason.postmanager.controller;

import com.plamason.postmanager.service.InstagramApiService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/insta")
public class InstagramApiController {
    
    private final InstagramApiService instagramApiService;
    
    public InstagramApiController(InstagramApiService instagramApiService) {
        this.instagramApiService = instagramApiService;
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok(instagramApiService.test());
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        return ResponseEntity.ok(instagramApiService.validateToken(token));
    }

    @PostMapping("/post")
    public ResponseEntity<?> createPost(
            @RequestParam String token,
            @RequestParam String content,
            @RequestParam(required = false) String imageUrl) {
        return ResponseEntity.ok(instagramApiService.createPost(token, content, imageUrl));
    }
} 