package com.spring.ai.demo03.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Prompt 模板：用变量占位符动态构建 Prompt
 * 对应文章「二、怎么用」的模板用法部分
 */
@RestController
class TemplateController {

    private final ChatClient chatClient;

    public TemplateController(ChatClient.Builder chatClientBuilder) {
        // 系统提示词也支持模板变量
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个{subject}老师，用通俗易懂的语言回答学生的问题。")
                .build();
    }

    /**
     * 在系统提示词中使用模板变量
     * 示例：GET /template/system?subject=物理&question=什么是重力
     */
    @GetMapping("/template/system")
    String systemTemplate(
            @RequestParam(defaultValue = "数学") String subject,
            @RequestParam(defaultValue = "什么是勾股定理") String question) {
        return chatClient.prompt()
                // 运行时替换系统提示词中的 {subject}
                .system(s -> s.param("subject", subject))
                .user(question)
                .call()
                .content();
    }

    /**
     * 在用户消息中使用模板变量
     * 示例：GET /template/user?topic=Python&level=初中
     */
    @GetMapping("/template/user")
    String userTemplate(
            @RequestParam(defaultValue = "Java") String topic,
            @RequestParam(defaultValue = "初学者") String level) {
        return chatClient.prompt()
                .system(s -> s.param("subject", "编程"))
                .user(u -> u
                        .text("请给一个{level}解释一下{topic}是什么，" +
                                "举一个生活中的例子帮助理解。")
                        .params(Map.of("level", level, "topic", topic)))
                .call()
                .content();
    }

    /**
     * 同时使用系统和用户模板变量
     * 示例：GET /template/both?subject=英语&topic=现在完成时
     */
    @GetMapping("/template/both")
    String bothTemplate(
            @RequestParam(defaultValue = "英语") String subject,
            @RequestParam(defaultValue = "现在完成时") String topic) {
        return chatClient.prompt()
                .system(s -> s
                        .text("你是一个{subject}老师，回答要包含：1.概念解释 2.例句 3.常见错误")
                        .param("subject", subject))
                .user(u -> u
                        .text("请讲解{topic}的用法")
                        .param("topic", topic))
                .call()
                .content();
    }
}
