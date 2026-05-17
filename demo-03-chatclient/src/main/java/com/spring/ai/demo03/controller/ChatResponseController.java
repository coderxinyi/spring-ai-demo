package com.spring.ai.demo03.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * ChatResponse 元数据：获取 token 用量等详细信息
 * 对应文章「二、怎么用」的 ChatResponse 部分
 */
@RestController
class ChatResponseController {

    private final ChatClient chatClient;

    public ChatResponseController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个教育领域的 AI 助手，回答要专业且易懂。")
                .build();
    }

    /**
     * 获取完整 ChatResponse（含 token 用量等元数据）
     * 示例：GET /response/detail?question=什么是机器学习
     */
    @GetMapping("/response/detail")
    Map<String, Object> detail(
            @RequestParam(defaultValue = "什么是机器学习？用三句话解释") String question) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(question)
                .call()
                .chatResponse();

        // 提取关键信息
        String content = chatResponse.getResult().getOutput().getText();
        var metadata = chatResponse.getMetadata();

        return Map.of(
                "content", content,
                "model", metadata.getModel() != null ? metadata.getModel() : "unknown",
                "usage", Map.of(
                        "promptTokens", metadata.getUsage().getPromptTokens(),
                        "completionTokens", metadata.getUsage().getCompletionTokens(),
                        "totalTokens", metadata.getUsage().getTotalTokens()
                )
        );
    }

    /**
     * 同时获取响应内容和结构化实体
     * 示例：GET /response/entity?topic=勾股定理
     */
    @GetMapping("/response/entity")
    Map<String, Object> responseEntity(
            @RequestParam(defaultValue = "勾股定理") String topic) {
        var responseEntity = chatClient.prompt()
                .user("用一句话解释" + topic + "，并给出难度评级（简单/中等/困难）")
                .call()
                .responseEntity(TopicExplanation.class);

        return Map.of(
                "entity", responseEntity.entity(),
                "model", responseEntity.response().getMetadata().getModel() != null
                        ? responseEntity.response().getMetadata().getModel() : "unknown"
        );
    }

    record TopicExplanation(
            String explanation,
            String difficultyLevel
    ) {}
}
