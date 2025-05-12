package com.plamason.postmanager.repository;

import com.plamason.postmanager.entity.Post;
import com.plamason.postmanager.entity.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByOrderByIdDesc(Pageable pageable);

    List<Post> findAllByIdLessThanOrderByIdDesc(Long id, Pageable pageable);

    List<Post> findByStatusOrderByCreatedAtAsc(PostStatus status);
}