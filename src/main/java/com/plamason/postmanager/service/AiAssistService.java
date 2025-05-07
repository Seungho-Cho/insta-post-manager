package com.plamason.postmanager.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AiAssistService {
    List<String> getBaseTag();
    List<String> suggestTag(String content);
    String translateContent(String content);
}
