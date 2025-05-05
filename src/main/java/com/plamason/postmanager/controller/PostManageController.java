package com.plamason.postmanager.controller;

import com.plamason.postmanager.dto.PostRequest;
import com.plamason.postmanager.dto.PostResponse;
import com.plamason.postmanager.entity.PostStatus;
import com.plamason.postmanager.service.PostManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostManageController {

    private final PostManageService postManageService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestBody PostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postManageService.createPost(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postManageService.getPost(id));
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(
            @RequestParam(required = false) Long lastPostId,
            @RequestParam(defaultValue = "10") int size) {
        List<PostResponse> posts = postManageService.getPosts(lastPostId, size);
        return ResponseEntity.ok(posts);
    }


    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @RequestBody PostRequest request) {
        return ResponseEntity.ok(postManageService.updatePost(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postManageService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PostResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam PostStatus status) {
        return ResponseEntity.ok(postManageService.updateStatus(id, status));
    }
} 