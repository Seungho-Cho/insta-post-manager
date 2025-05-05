package com.plamason.postmanager.controller;

import com.plamason.postmanager.dto.AppSettingRequest;
import com.plamason.postmanager.entity.AppSetting;
import com.plamason.postmanager.service.AppSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/api/settings")
@RequiredArgsConstructor
public class AppSettingController {

    private final AppSettingService appSettingService;

    @GetMapping
    public ResponseEntity<Map<String, String>> getAllSettings() {
        return ResponseEntity.ok(appSettingService.getAllSettings());
    }

    @GetMapping("/{key}")
    public ResponseEntity<AppSetting> getSetting(@PathVariable String key) {
        return ResponseEntity.ok(appSettingService.getSetting(key));
    }

    @PostMapping
    public ResponseEntity<Void> createSetting(@RequestBody AppSettingRequest request) {
        appSettingService.addSetting(request.getSettingKey(), request.getSettingValue());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{key}")
    public ResponseEntity<Void> updateSetting(
            @PathVariable String key,
            @RequestBody AppSettingRequest request) {
        appSettingService.updateSetting(key, request.getSettingValue());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteSetting(@PathVariable String key) {
        appSettingService.deleteSetting(key);
        return ResponseEntity.noContent().build();
    }
}


