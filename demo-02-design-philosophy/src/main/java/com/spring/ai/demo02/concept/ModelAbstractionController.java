package com.spring.ai.demo02.concept;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 演示 Spring AI 最核心的抽象：ChatModel 接口
 * 这个接口是所有对话模型实现的统一契约
 */
@RestController
class ModelAbstractionController {

    private final ChatModel chatModel;

    // 直接注入 ChatModel 接口，不关心底层是 DeepSeek 还是 OpenAI
    public ModelAbstractionController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 用 Prompt 对象调用 ChatModel
     * Prompt 包含多条 Message，每条 Message 有不同的角色
     * 示例：GET /concept/prompt?question=什么是深度学习
     */
    @GetMapping("/concept/prompt")
    String chatWithPrompt(@RequestParam String question) {
        // 构造 Prompt：系统消息 + 用户消息
        Prompt prompt = new Prompt(List.of(
                new SystemMessage("你是一个教育领域的 AI 助手，回答要专业且易懂。"),
                new UserMessage(question)
        ));

        // 调用 ChatModel 接口，返回 ChatResponse
        ChatResponse response = chatModel.call(prompt);

        // 从响应中提取文本内容
        return response.getResult().getOutput().getText();
    }

    /**
     * 用最简单的方式调用 ChatModel
     * ChatModel 提供了便捷方法，一行搞定
     * 示例：GET /concept/simple?question=什么是机器学习
     */
    @GetMapping("/concept/simple")
    String chatSimple(@RequestParam(defaultValue = "什么是机器学习？") String question) {
        // ChatModel 提供了 call(String) 的便捷方法
        return chatModel.call(question);
    }
}
