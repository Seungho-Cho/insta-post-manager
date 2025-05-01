package com.plamason.postmanager.service;

import com.plamason.postmanager.exception.InstaPostFailedException;
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
    private static final String BASE_URL = "https://graph.instagram.com/v22.0/";

    @Override
    public String test() {
        return "test";
    }

    @Override
    public Object validateToken(String token) {
        return null;
    }

    @Override
    public String createPost(String userId, String accessToken, String imageUrl, String content) {
        String containerId = createContainer(userId, accessToken, imageUrl, content, false);
        return postContainer(userId, accessToken, containerId);
    }

    @Override
    public String createCarouselPost(String userId, String accessToken, List<String> imageUrlList, String content) {
        List<String> containerIdList = new ArrayList<>();
        imageUrlList.forEach(imageUrl ->
            containerIdList.add(createContainer(userId, accessToken, imageUrl, content, true))
        );
        String containerId = createCarouselContainer(userId, accessToken, containerIdList, content);
        return postContainer(userId, accessToken, containerId);
    }

    private String createContainer(String userId, String accessToken, String imageUrl, String content, boolean isCarousel) {
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

        try {
            return getIdFromResponse(postWebClient(mediaUrl, mediaBody));
        } catch (InstaPostFailedException e) {
            return null;
        }
    }

    private String postContainer(String userId, String accessToken, String containerId) {
        validateNotNull(userId, "userId");
        validateNotNull(accessToken, "accessToken");
        validateNotNull(containerId, "containerId");

        String publishUrl = BASE_URL + userId + "/media_publish";
        Map<String, Object> publishBody = new HashMap<>();
        publishBody.put("creation_id", containerId);
        publishBody.put("access_token", accessToken);

        try {
            return getIdFromResponse(postWebClient(publishUrl,publishBody));
        } catch (InstaPostFailedException e) {
            return null;
        }
    }

    private String createCarouselContainer(String userId, String accessToken, List<String> containerIdList, String content) {
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

        try {
            return getIdFromResponse(postWebClient(mediaUrl, mediaBody));
        } catch (InstaPostFailedException e) {
            return null;
        }
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
