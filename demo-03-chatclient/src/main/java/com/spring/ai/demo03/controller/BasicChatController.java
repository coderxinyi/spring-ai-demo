package com.spring.ai.demo03.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 基础对话：同步调用 + 流式调用
 * 对应文章「二、怎么用」的基础用法部分
 */
@RestController
class BasicChatController {

    private final ChatClient chatClient;

    public BasicChatController(ChatClient.Builder chatClientBuilder) {
        // 构建时设置默认系统提示词
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个耐心的学习助手，专门帮助中小学生解答学习中的疑问。" +
                        "回答要简洁易懂，适合学生理解。")
                .build();
    }

    /**
     * 同步对话 - 等待 AI 完整回答后返回
     * 示例：GET /basic/chat?question=什么是光合作用
     */
    @GetMapping("/basic/chat")
    String chat(@RequestParam(defaultValue = "你好，请介绍一下你自己") String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    /**
     * 流式对话 - 边生成边返回，打字机效果
     * 示例：GET /basic/stream?question=请解释牛顿第三定律
     */
    @GetMapping(value = "/basic/stream", produces = "text/event-stream;charset=UTF-8")
    Flux<String> stream(@RequestParam(defaultValue = "你好") String question) {
        return chatClient.prompt()
                .user(question)
                .stream()
                .content();
    }

    /**
     * 用 prompt(String) 简写方式调用
     * 示例：GET /basic/quick?q=1+1等于几
     */
    @GetMapping("/basic/quick")
    String quickChat(@RequestParam(defaultValue = "1+1等于几？") String q) {
        // prompt(String) 等价于 prompt().user(content)
        return chatClient.prompt(q)
                .call()
                .content();
    }
}
