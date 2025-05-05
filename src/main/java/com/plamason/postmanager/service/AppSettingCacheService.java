package com.plamason.postmanager.service;

import com.plamason.postmanager.entity.AppSetting;
import com.plamason.postmanager.repository.AppSettingRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AppSettingCacheService {
    private final AppSettingRepository appSettingRepository;

    @Cacheable("appSettings")
    public Map<String, String> getCachedSettings() {
        return appSettingRepository.findAll()
                .stream()
                .collect(Collectors.toMap(AppSetting::getSettingKey, AppSetting::getSettingValue));
    }

    public String getSettingValue(String key) {
        return Optional.ofNullable(getCachedSettings().get(key))
                .orElseThrow(() -> new RuntimeException("Setting not found: " + key));
    }

    @CacheEvict(value = "appSettings", allEntries = true)
    public void evictCache() { }
}
