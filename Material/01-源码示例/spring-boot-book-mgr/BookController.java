package com.example.bookmgr.controller;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 演示素材：Spring Boot 图书管理 REST API 片段
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    @GetMapping
    public List<Map<String, Object>> list() {
        return List.of(
                Map.of("id", 1, "title", "Java 核心技术", "isbn", "978-7-111-54742-6"),
                Map.of("id", 2, "title", "Spring Boot 实战", "isbn", "978-7-115-41967-8")
        );
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, String> body) {
        return Map.of("id", 3, "title", body.getOrDefault("title", "未命名"), "status", "created");
    }
}
