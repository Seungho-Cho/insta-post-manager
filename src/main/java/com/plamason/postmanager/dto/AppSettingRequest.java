package com.plamason.postmanager.dto;

import lombok.Data;

@Data
public class AppSettingRequest {
    private String settingKey;
    private String settingValue;
}
