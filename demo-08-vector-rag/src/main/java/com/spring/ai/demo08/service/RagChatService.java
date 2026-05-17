package com.spring.ai.demo08.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

/**
 * RAG 问答服务
 * 使用 QuestionAnswerAdvisor 实现开箱即用的检索增强生成
 */
@Service
public class RagChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagChatService(ChatModel chatModel, VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        // 构建 ChatClient，默认挂载 QuestionAnswerAdvisor
        // Advisor 的工作流程：拦截用户问题 -> 向量搜索 -> 拼接到 Prompt -> 发给 LLM
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .similarityThreshold(0.5)
                                .topK(5)
                                .build())
                        .build())
                .build();
    }

    /**
     * RAG 问答：自动检索相关文档 + LLM 生成回答
     */
    public String ask(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}
