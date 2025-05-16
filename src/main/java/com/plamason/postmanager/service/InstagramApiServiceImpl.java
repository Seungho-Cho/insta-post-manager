package com.plamason.postmanager.service;

import com.plamason.postmanager.enums.AppSettingName;
import com.plamason.postmanager.exception.InstaPostFailedException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstagramApiServiceImpl implements InstagramApiService {

    private final WebClient webClient;
    private final AppSettingService appSettingService;

    private static final String BASE_URL = "https://graph.instagram.com/v22.0/";

    private String ACCES_TOKEN;
    private String USER_ID;

    @PostConstruct
    public void init() {
        Map<String, String> appSettings = appSettingService.getAllSettings();
        ACCES_TOKEN = appSettings.get(AppSettingName.INSTAGRAM_API_ACCESS_TOKEN.toString());
        USER_ID = appSettings.get(AppSettingName.INSTAGRAM_API_USER_ID.toString());

        if (ACCES_TOKEN == null || ACCES_TOKEN.isBlank()) {
            throw new IllegalStateException("INSTAGRAM access token is not configured");
        }
        if (USER_ID == null || USER_ID.isBlank()) {
            throw new IllegalStateException("INSTAGRAM user id is not configured");
        }
    }

    @Override
    public String test() {
        return "test";
    }

    @Override
    public String validateToken(String token) {
        return null;
    }

    @Override
    public String createPost(String imageUrl, String content) throws InstaPostFailedException {
        String containerId = createContainer(USER_ID, ACCES_TOKEN, imageUrl, content, false);
        return postContainer(USER_ID, ACCES_TOKEN, containerId);
    }

    @Override
    public String createCarouselPost(List<String> imageUrlList, String content) throws InstaPostFailedException {
        List<String> containerIdList = new ArrayList<>();
        imageUrlList.forEach(imageUrl -> {
            try {
                containerIdList.add(createContainer(USER_ID, ACCES_TOKEN, imageUrl, content, true));
            } catch (InstaPostFailedException e) {
                throw new RuntimeException("Failed to create container for image URL: " + imageUrl, e);
            }
        });
        String containerId = createCarouselContainer(USER_ID, ACCES_TOKEN, containerIdList, content);
        return postContainer(USER_ID, ACCES_TOKEN, containerId);
    }

    private String createContainer(String userId, String accessToken, String imageUrl, String content, boolean isCarousel) throws InstaPostFailedException {
        validateNotNull(userId, "userId");
        validateNotNull(accessToken, "accessToken");
        validateNotNull(imageUrl, "imageUrl");

        String mediaUrl = BASE_URL + userId + "/media";

        Map<String, Object> mediaBody = new HashMap<>();
        mediaBody.put("domain", "INSTAGRAM");
        mediaBody.put("access_token", accessToken);
        mediaBody.put("image_url", imageUrl);
        if (isCarousel) {
            mediaBody.put("is_carousel_item", true);
        } else if (content != null && !content.trim().isEmpty()) {
            mediaBody.put("caption", content);
        }

        return getIdFromResponse(postWebClient(mediaUrl, mediaBody));
    }

    private String postContainer(String userId, String accessToken, String containerId) throws InstaPostFailedException {
        validateNotNull(userId, "userId");
        validateNotNull(accessToken, "accessToken");
        validateNotNull(containerId, "containerId");

        String publishUrl = BASE_URL + userId + "/media_publish";
        Map<String, Object> publishBody = new HashMap<>();
        publishBody.put("creation_id", containerId);
        publishBody.put("access_token", accessToken);

        return getIdFromResponse(postWebClient(publishUrl,publishBody));
    }

    private String createCarouselContainer(String userId, String accessToken, List<String> containerIdList, String content) throws InstaPostFailedException {
        validateNotNull(userId, "userId");
        validateNotNull(accessToken, "accessToken");
        validateNotNull(containerIdList, "containerId");

        String mediaUrl = BASE_URL + userId + "/media";

        Map<String, Object> mediaBody = new HashMap<>();
        mediaBody.put("domain", "INSTAGRAM");
        mediaBody.put("media_type", "CAROUSEL");
        mediaBody.put("children", containerIdList);
        mediaBody.put("access_token", accessToken);
        mediaBody.put("caption", content);

        return getIdFromResponse(postWebClient(mediaUrl, mediaBody));
    }

    private void validateNotNull(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + "는 null일 수 없습니다");
        }
    }

    private Map<String, Object> postWebClient(String uri, Map<String, Object> mediaBody) throws InstaPostFailedException {
        log.info("Sending POST request to {}, body: {}", uri, mediaBody);
        return webClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mediaBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .blockOptional()
                .map(response -> {
                    log.info("Response from Instagram: {}", response);
                    return response;
                })
                .orElseThrow(() -> new InstaPostFailedException("Instagram API 응답이 없습니다."));
    }

    private String getIdFromResponse(Map<String, Object> response) throws InstaPostFailedException {
        return Optional.ofNullable(response.get("id"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .orElseThrow(() -> new InstaPostFailedException(response.toString()));
    }

}
