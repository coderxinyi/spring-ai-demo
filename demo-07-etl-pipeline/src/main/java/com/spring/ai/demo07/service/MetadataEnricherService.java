package com.spring.ai.demo07.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 元数据增强服务 — 使用 AI 模型提取文档关键词
 */
@Service
public class MetadataEnricherService {

    private final ChatModel chatModel;

    public MetadataEnricherService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 使用 KeywordMetadataEnricher 为文档提取关键词
     * 提取的关键词会存储到文档的 metadata 中，key 为 "excerpt_keywords"
     */
    public List<Document> enrichWithKeywords(List<Document> documents) {
        KeywordMetadataEnricher enricher = KeywordMetadataEnricher.builder(chatModel)
                .keywordCount(5)    // 每个文档提取5个关键词
                .build();

        return enricher.apply(documents);
    }
}
