package com.spring.ai.demo10.controller;

import com.spring.ai.demo10.service.StudentAssistantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 学习助手接口
 */
@RestController
public class StudentAssistantController {

    private final StudentAssistantService service;

    public StudentAssistantController(StudentAssistantService service) {
        this.service = service;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return service.chat(message);
    }
}
