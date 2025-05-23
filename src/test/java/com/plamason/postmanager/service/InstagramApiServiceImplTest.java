package com.plamason.postmanager.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class InstagramApiServiceImplTest {

    @Autowired
    private InstagramApiService instagramApiService;

    private static final String USER_ID = "";
    private static final String ACCESS_TOKEN = "";
    private static final String IMAGE_URL = "https://upload.wikimedia.org/wikipedia/commons/9/9a/Gull_portrait_ca_usa.jpg";
    private static final String CONTENT = "Test content #test #api_test";

    @Test
    void createPost() {
        instagramApiService.createPost(USER_ID, ACCESS_TOKEN, IMAGE_URL, CONTENT);
    }

    @Test
    void createCarouselPost() {
        List<String> imageList = new ArrayList<>();
        imageList.add(IMAGE_URL);
        imageList.add(IMAGE_URL);
        imageList.add(IMAGE_URL);
        imageList.add(IMAGE_URL);
        instagramApiService.createCarouselPost(USER_ID, ACCESS_TOKEN, imageList, CONTENT);
    }
} 