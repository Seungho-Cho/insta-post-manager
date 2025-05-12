package com.plamason.postmanager.controller;

import com.plamason.postmanager.scheduler.PostScheduler;
import com.plamason.postmanager.service.AdminService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {
    private final PostScheduler postScheduler;

    @PostMapping("/schedule/execute")
    public ResponseEntity<String> executePostingManually() {
        try {
            postScheduler.executePostingManually();
            return ResponseEntity.ok("포스팅 작업이 수동으로 실행되었습니다.");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("포스팅 작업 실행 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

} 