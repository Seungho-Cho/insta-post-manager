package com.plamason.postmanager.repository;

import com.plamason.postmanager.entity.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppSettingRepository extends JpaRepository<AppSetting, Long> {
    Optional<AppSetting> findBySettingKey(String key);

    void deleteBySettingKey(String settingKey);
    boolean existsBySettingKey(String key);
}
