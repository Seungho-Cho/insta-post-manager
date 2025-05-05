package com.plamason.postmanager.service;

import com.plamason.postmanager.dto.PostRequest;
import com.plamason.postmanager.dto.PostResponse;
import com.plamason.postmanager.entity.Post;
import com.plamason.postmanager.entity.PostStatus;
import com.plamason.postmanager.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostManageServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostManageService postService;

    private PostRequest postRequest;
    private Post post;
    private PostResponse postResponse;

    @BeforeEach
    void setUp() {
        postRequest = PostRequest.builder()
                .title("Test Title")
                .content("Test Content")
                .imageUrls(Arrays.asList("image1.jpg", "image2.jpg"))
                .tags(Arrays.asList("tag1", "tag2"))
                .author("Test Author")
                .build();

        post = Post.builder()
                .id(1L)
                .title("Test Title")
                .content("Test Content")
                .imageUrls(Arrays.asList("image1.jpg", "image2.jpg"))
                .tags(Arrays.asList("tag1", "tag2"))
                .author("Test Author")
                .status(PostStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();

        postResponse = PostResponse.builder()
                .id(1L)
                .title("Test Title")
                .content("Test Content")
                .imageUrls(Arrays.asList("image1.jpg", "image2.jpg"))
                .tags(Arrays.asList("tag1", "tag2"))
                .author("Test Author")
                .status(PostStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("게시물 생성 성공")
    void createPost_Success() {
        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostResponse response = postService.createPost(postRequest);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo(postRequest.getTitle());
        assertThat(response.getContent()).isEqualTo(postRequest.getContent());
        assertThat(response.getStatus()).isEqualTo(PostStatus.DRAFT);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("게시물 조회 성공")
    void getPost_Success() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        PostResponse response = postService.getPost(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo(post.getTitle());
    }

    @Test
    @DisplayName("존재하지 않는 게시물 조회 실패")
    void getPost_NotFound_Failure() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPost(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    /*@Test
    @DisplayName("전체 게시물 조회 성공")
    void getAllPosts_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(Arrays.asList(post));
        when(postRepository.findAll(pageable)).thenReturn(postPage);

        Page<PostResponse> response = postService.getAllPosts(pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo(post.getTitle());
    }*/

    @Test
    @DisplayName("게시물 수정 성공")
    void updatePost_Success() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostResponse response = postService.updatePost(1L, postRequest);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo(postRequest.getTitle());
        assertThat(response.getContent()).isEqualTo(postRequest.getContent());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("존재하지 않는 게시물 수정 실패")
    void updatePost_NotFound_Failure() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.updatePost(1L, postRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("게시물 삭제 성공")
    void deletePost_Success() {
        when(postRepository.existsById(1L)).thenReturn(true);
        doNothing().when(postRepository).deleteById(1L);

        postService.deletePost(1L);

        verify(postRepository).deleteById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 게시물 삭제 실패")
    void deletePost_NotFound_Failure() {
        when(postRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> postService.deletePost(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("게시물 상태 업데이트 성공")
    void updateStatus_Success() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostResponse response = postService.updateStatus(1L, PostStatus.PUBLISHED);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        verify(postRepository).save(any(Post.class));
    }
} 