package com.plamason.postmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class UiController {
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Post Manager");
        return "layout";
    }

    @GetMapping("/ui/posts")
    public String getPostsPage() {
        return "post/list";
    }

    @GetMapping("/ui/posts/{id}")
    public String editPostPage(@PathVariable Long id, Model model) {
        model.addAttribute("id", id);
        return "post/edit";
    }
}