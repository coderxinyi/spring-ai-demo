package com.spring.ai.demo04.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.options.ChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模式 1-2：Zero-Shot 和 Few-Shot
 * 最基础的两种 Prompt 模式
 */
@RestController
@RequestMapping("/prompt")
class BasicPatternsController {

    private final ChatClient chatClient;

    public BasicPatternsController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个教育领域的智能助手，帮助分析学生数据和课程信息。")
                .build();
    }

    // ========== 模式 1：Zero-Shot（零样本提示） ==========

    /**
     * 不给任何例子，直接让 AI 分类学生反馈的情感
     * 适用场景：简单分类、翻译等模型训练时大概率见过的任务
     */
    @GetMapping("/zero-shot")
    SentimentResult zeroShot(
            @RequestParam(defaultValue = "这个物理实验太有意思了，终于理解了光的折射原理") String feedback) {
        return chatClient.prompt()
                .user(u -> u.text("""
                        请判断以下学生反馈的情感倾向，只返回 POSITIVE、NEUTRAL 或 NEGATIVE。

                        学生反馈：{feedback}
                        """).param("feedback", feedback))
                .options(ChatOptions.builder()
                        .temperature(0.1)  // 分类任务用低温度，确保确定性
                        .maxTokens(10)
                        .build())
                .call()
                .entity(SentimentResult.class);
    }

    record SentimentResult(Sentiment sentiment) {
        enum Sentiment { POSITIVE, NEUTRAL, NEGATIVE }
    }

    // ========== 模式 2：Few-Shot（少样本提示） ==========

    /**
     * 给几个例子，让 AI 按照指定格式解析学生选课信息
     * 适用场景：需要特定输出格式、任务比较复杂、零样本效果不好时
     */
    @GetMapping("/few-shot")
    String fewShot(
            @RequestParam(defaultValue = "我想选高二的物理和化学") String order) {
        return chatClient.prompt()
                .user(u -> u.text("""
                        将学生的选课请求解析为 JSON 格式。

                        例子 1：
                        学生说：我要选高一的数学和英语
                        结果：
                        ```json
                        {
                            "grade": "高一",
                            "subjects": ["数学", "英语"]
                        }
                        ```

                        例子 2：
                        学生说：帮我把高三生物换成地理
                        结果：
                        ```json
                        {
                            "grade": "高三",
                            "subjects": ["地理"],
                            "action": "replace",
                            "replaced": "生物"
                        }
                        ```

                        学生说：{order}
                        """).param("order", order))
                .options(ChatOptions.builder()
                        .temperature(0.1)
                        .maxTokens(200)
                        .build())
                .call()
                .content();
    }
}
