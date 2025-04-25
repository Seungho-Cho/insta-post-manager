package com.plamason.postmanager.controller;

import com.plamason.postmanager.service.AdminService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {
    
    private final AdminService adminService;
    
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

} 