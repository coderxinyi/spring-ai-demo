package com.spring.ai.demo10.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

/**
 * AI 学习助手服务
 * 通过 MCP Client 自动发现远程工具并注册到 ChatClient
 */
@Service
public class StudentAssistantService {

    private final ChatClient chatClient;

    public StudentAssistantService(ChatModel chatModel,
                                   ToolCallbackProvider mcpToolProvider) {
        // 将 MCP Client 发现的工具注册到 ChatClient
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem("你是一个教育培训行业的 AI 学习助手。" +
                        "当学生询问课程、成绩等需要实时数据的问题时，" +
                        "请使用提供的工具来获取信息，不要编造数据。" +
                        "每次回答最多调用 3 次工具，避免重复调用。")
                .defaultToolCallbacks(mcpToolProvider)
                .build();
    }

    public String chat(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }
}
