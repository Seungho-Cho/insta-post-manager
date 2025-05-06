package com.plamason.postmanager.controller;

import com.plamason.postmanager.dto.TagSuggestRequest;
import com.plamason.postmanager.service.TagSuggestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
public class TagSuggestController {

    private final TagSuggestService tagSuggestService;

    @GetMapping("/baseTag")
    public List<String> getBaseTag() {
        return tagSuggestService.getBaseTag();
    }

    @PostMapping("/suggest")
    public List<String> suggestTag(@RequestBody TagSuggestRequest request) {
        return tagSuggestService.suggestTag(
                request.getTitle()
                    + " " + request.getContent()
                    + " " + request.getDescription());
    }

}
