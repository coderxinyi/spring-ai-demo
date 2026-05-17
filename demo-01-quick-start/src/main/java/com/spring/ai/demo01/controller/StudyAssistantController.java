package com.spring.ai.demo01.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI 学习助手 - 最简单的对话接口
 */
@RestController
class StudyAssistantController {

    private final ChatClient chatClient;

    public StudyAssistantController(ChatClient.Builder chatClientBuilder) {
        // 构建时设置系统提示词，让 AI 扮演学习助手
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个耐心的学习助手，专门帮助中小学生解答学习中的疑问。" +
                        "回答要简洁易懂，适合学生理解。如果学生问了非学习相关的问题，" +
                        "委婉引导他们回到学习话题上。")
                .build();
    }

    /**
     * 同步对话接口 - 等待 AI 完整回答后返回
     * 示例：GET /chat?question=什么是光合作用
     */
    @GetMapping("/chat")
    String chat(@RequestParam(defaultValue = "你好，请介绍一下你自己") String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    /**
     * 流式对话接口 - 边生成边返回，打字机效果
     * 示例：GET /stream?question=请解释牛顿第三定律
     */
    @GetMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    Flux<String> stream(@RequestParam(defaultValue = "你好") String question) {
        return chatClient.prompt()
                .user(question)
                .stream()
                .content();
    }
}
