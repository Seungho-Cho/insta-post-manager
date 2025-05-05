package com.plamason.postmanager.service;

import com.plamason.postmanager.entity.AppSetting;
import com.plamason.postmanager.repository.AppSettingRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class AppSettingService {

    private final AppSettingRepository appSettingRepository;
    private final AppSettingCacheService appSettingCacheService;


    public Map<String, String> getAllSettings() {
        return appSettingCacheService.getCachedSettings();
    }

    public void addSetting(String key, String value) {
        if (appSettingRepository.existsBySettingKey(key)) {
            throw new RuntimeException("Setting already exists: " + key);
        }
        AppSetting setting = AppSetting.builder()
                .settingKey(key)
                .settingValue(value)
                .build();
        appSettingRepository.save(setting);
        appSettingCacheService.evictCache();
    }

    public void updateSetting(String key, String value) {
        AppSetting setting = appSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new RuntimeException("Setting not found"));
        setting.setSettingValue(value);
        appSettingRepository.save(setting);
        appSettingCacheService.evictCache();
    }

    public void deleteSetting(String key) {
        appSettingRepository.deleteBySettingKey(key);
        appSettingCacheService.evictCache();
    }

    public AppSetting getSetting(String key) {
        return appSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new RuntimeException("Setting not found"));
    }
}
