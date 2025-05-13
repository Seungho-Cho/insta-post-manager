package com.plamason.postmanager.scheduler;

import com.plamason.postmanager.entity.AppSetting;
import com.plamason.postmanager.entity.Post;
import com.plamason.postmanager.entity.PostStatus;
import com.plamason.postmanager.enums.AppSettingName;
import com.plamason.postmanager.repository.AppSettingRepository;
import com.plamason.postmanager.repository.PostRepository;
import com.plamason.postmanager.service.InstagramApiService;
import com.plamason.postmanager.service.PostManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostScheduler {

    private final PostRepository postRepository;
    private final PostManageService postManageService;
    private final AppSettingRepository appSettingRepository;
    private final InstagramApiService instagramApiService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // 스케줄러에서 사용할 메서드
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void schedulePosting() {
        executePosting(false);
    }

    // 수동 실행을 위한 메서드
    @Transactional
    public void executePostingManually() {
        executePosting(true);
    }

    // 실제 포스팅 로직
    private void executePosting(boolean isManual) {
        log.info("포스팅 작업 시작 ({}): {}", 
                isManual ? "수동실행" : "스케줄실행", 
                LocalDateTime.now());
        
        if (!isManual) {
            // 자동 실행일 경우에만 시간 체크
            AppSetting postingTimeSetting = appSettingRepository.findBySettingKey(AppSettingName.POST_MANAGER_SCHEDULE_HOUR.toString())
                    .orElseGet(() -> {
                        AppSetting defaultSetting = new AppSetting();
                        defaultSetting.setSettingKey(AppSettingName.POST_MANAGER_SCHEDULE_HOUR.toString());
                        defaultSetting.setSettingValue("18:00");
                        return appSettingRepository.save(defaultSetting);
                    });

            LocalTime scheduledTime = LocalTime.parse(postingTimeSetting.getSettingValue(), TIME_FORMATTER);
            LocalTime currentTime = LocalTime.now();

            if (currentTime.getHour() != scheduledTime.getHour()) {
                log.info("현재 시간은 예약된 시간이 아닙니다. 작업을 건너뜁니다.");
                return;
            }
        }

        // SCHEDULED 상태인 포스트들 조회
        List<Post> scheduledPosts = postRepository.findByStatusOrderByCreatedAtAsc(PostStatus.SCHEDULED);
        
        if (scheduledPosts.isEmpty()) {
            log.info("예약된 포스트가 없습니다.");
            return;
        }

        Post post = scheduledPosts.getFirst();
        log.info("포스트 처리 시작: {}", post.getId());

        try {
            log.info("포스트 게시 시작 - Post ID: {}, Title: {}", post.getId(), post.getTitle());

            String postContent = generatePostContent(post);
            String instaPostId;
            if(post.getImageUrls().size() == 1) {
                instaPostId = instagramApiService.createPost(post.getImageUrls().getFirst(), postContent);
            } else {
                instaPostId = instagramApiService.createCarouselPost(post.getImageUrls(), postContent);
            }

            post.setInstagramPostId(instaPostId);
            post.setStatus(PostStatus.PUBLISHED);
            postManageService.updateStatus(post.getId(), PostStatus.PUBLISHED);
            
            log.info("포스트 게시 완료 - Post ID: {}", post.getId());
        } catch (Exception e) {
            log.error("포스트 게시 실패 - Post ID: {}, 에러: {}", post.getId(), e.getMessage(), e);
            post.setStatus(PostStatus.FAILED);
            post.setErrorMessage(e.getMessage());
            postRepository.save(post);
        }
    }

    private String generatePostContent(Post post) {
        return """
            %s
            
            %s
            
            %s
            """.formatted(
                post.getTitle(),
                post.getAuthor() == null || post.getAuthor().isBlank()
                        ? ""
                        : "Builder: " + post.getAuthor(),
                post.getContent() + "\n" + post.getTags().stream()
                        .map("#%s"::formatted)
                        .collect(Collectors.joining("\n"))
        ).strip(); // strip()를 이용해 불필요한 공백 제거
    }
}