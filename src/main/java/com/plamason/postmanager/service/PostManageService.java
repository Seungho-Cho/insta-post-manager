package com.plamason.postmanager.service;

import com.plamason.postmanager.dto.PostRequest;
import com.plamason.postmanager.dto.PostResponse;
import com.plamason.postmanager.entity.Post;
import com.plamason.postmanager.entity.PostStatus;
import com.plamason.postmanager.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostManageService {
    private final PostRepository postRepository;

    @Transactional
    public PostResponse createPost(PostRequest request) {
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .imageUrls(request.getImageUrls())
                .tags(request.getTags())
                .author(request.getAuthor())
                .status(PostStatus.DRAFT)
                .build();

        return convertToResponse(postRepository.save(post));
    }

    public PostResponse getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return convertToResponse(post);
    }

    public Page<PostResponse> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    @Transactional
    public PostResponse updatePost(Long id, PostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setImageUrls(request.getImageUrls());
        post.setTags(request.getTags());
        post.setAuthor(request.getAuthor());

        return convertToResponse(postRepository.save(post));
    }

    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new RuntimeException("Post not found");
        }
        postRepository.deleteById(id);
    }

    @Transactional
    public PostResponse updateStatus(Long id, PostStatus status) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setStatus(status);
        return convertToResponse(postRepository.save(post));
    }

    private PostResponse convertToResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .imageUrls(post.getImageUrls())
                .tags(post.getTags())
                .author(post.getAuthor())
                .createdAt(post.getCreatedAt())
                .postedAt(post.getPostedAt())
                .status(post.getStatus())
                .instagramPostId(post.getInstagramPostId())
                .errorMessage(post.getErrorMessage())
                .build();
    }
} 