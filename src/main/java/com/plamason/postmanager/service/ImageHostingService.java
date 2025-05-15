package com.plamason.postmanager.service;

import org.springframework.stereotype.Service;

@Service
public interface ImageHostingService {
    String uploadImage(String imageUrl, String saveName);
}
