package com.spring.ai.demo09.service;

import com.spring.ai.demo09.tools.StudentTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

/**
 * AI 学习助手服务
 * 注册工具并通过 ChatClient 提供对话能力
 */
@Service
public class StudentAssistantService {

    private final ChatClient chatClient;

    public StudentAssistantService(ChatModel chatModel, StudentTools studentTools) {
        // 在 Builder 中注册默认工具，所有请求都可用
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem("你是一个教育培训行业的 AI 学习助手。" +
                        "当学生询问课程、成绩等需要实时数据的问题时，" +
                        "请使用提供的工具来获取信息，不要编造数据。")
                .defaultTools(studentTools)
                .build();
    }

    public String chat(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }
}
