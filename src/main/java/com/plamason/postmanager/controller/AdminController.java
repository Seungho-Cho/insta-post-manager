package com.plamason.postmanager.controller;

import com.plamason.postmanager.service.AdminService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

public class AdminController {
    
    private final AdminService adminService;
    
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

} 