package com.spring.ai.demo04.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.options.ChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 模式 10：APE（Automatic Prompt Engineering，自动提示工程）
 * 让 AI 自己生成和优化 Prompt
 */
@RestController
@RequestMapping("/prompt")
class AutoPromptController {

    private final ChatClient chatClient;

    public AutoPromptController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个 Prompt 工程专家，擅长优化提示词。")
                .build();
    }

    /**
     * APE 模式：先让 AI 生成多个 Prompt 变体，再评估选出最优
     * 适用场景：训练数据增强、Prompt 质量优化、测试 AI 的理解鲁棒性
     */
    @GetMapping("/ape")
    Map<String, String> ape(
            @RequestParam(defaultValue = "帮我查一下下周的数学课表") String seedRequest) {
        // 第一步：生成多个 Prompt 变体
        String variants = chatClient.prompt()
                .user(u -> u.text("""
                        我们正在开发一个教育平台的智能助手。以下是用户的一个原始请求：
                        "{request}"

                        请生成 5 种不同的 Prompt 表述方式，保持相同语义但用不同措辞。
                        每种表述都应该能被 AI 助手准确理解并执行。
                        """).param("request", seedRequest))
                .options(ChatOptions.builder()
                        .temperature(1.0)  // 高温度，鼓励多样化生成
                        .build())
                .call()
                .content();

        // 第二步：评估并选出最优表述
        String bestPrompt = chatClient.prompt()
                .user(u -> u.text("""
                        以下是同一个请求的 5 种表述：
                        {variants}

                        请评估每种表述的清晰度、准确性和可执行性，
                        选出最优的一种，并说明为什么。
                        """).param("variants", variants))
                .call()
                .content();

        return Map.of(
                "original", seedRequest,
                "variants", variants,
                "recommendation", bestPrompt
        );
    }
}
