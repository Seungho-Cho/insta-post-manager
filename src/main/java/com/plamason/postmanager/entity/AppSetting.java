package com.plamason.postmanager.entity;

import jakarta.persistence.*;
import lombok.*;

@Table
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppSetting {
    @Id
    @Column(unique=true, nullable=false)
    private String settingKey;

    @Column(length = 1000)
    private String settingValue;
}
