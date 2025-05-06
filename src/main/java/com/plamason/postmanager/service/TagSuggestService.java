package com.plamason.postmanager.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TagSuggestService {
    List<String> getBaseTag();
    List<String> suggestTag(String content);
}
