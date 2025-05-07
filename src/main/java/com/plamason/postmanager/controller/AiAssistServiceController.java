package com.plamason.postmanager.controller;

import com.plamason.postmanager.dto.AiAssistRequest;
import com.plamason.postmanager.service.AiAssistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiAssistServiceController {

    private final AiAssistService aiAssistService;

    @GetMapping("/baseTag")
    public List<String> getBaseTag() {
        return aiAssistService.getBaseTag();
    }

    @PostMapping("/tagSuggest")
    public List<String> suggestTag(@RequestBody AiAssistRequest request) {
        return aiAssistService.suggestTag(
                request.getTitle()
                    + " " + request.getContent()
                    + " " + request.getDescription());
    }

    @PostMapping("/translate")
    public String translateContent(@RequestBody AiAssistRequest request) {
        return aiAssistService.translateContent(request.getContent());
    }

}
