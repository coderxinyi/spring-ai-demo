package com.spring.ai.demo08.controller;

import com.spring.ai.demo08.service.KnowledgeBaseService;
import com.spring.ai.demo08.service.RagChatService;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识库与 RAG 问答控制器
 */
@RestController
@RequestMapping("/knowledge")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final RagChatService ragChatService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService,
                                   RagChatService ragChatService) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.ragChatService = ragChatService;
    }

    /**
     * 加载文档到向量数据库
     * POST http://localhost:9008/knowledge/load
     */
    @PostMapping("/load")
    public Map<String, Object> loadDocuments() {
        int count = knowledgeBaseService.loadDocuments();
        return Map.of(
                "status", "success",
                "message", "成功加载 " + count + " 个文档块到向量数据库"
        );
    }

    /**
     * 相似度搜索
     * GET http://localhost:9008/knowledge/search?query=xxx&topK=3&threshold=0.5
     */
    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String query,
                                      @RequestParam(defaultValue = "3") int topK,
                                      @RequestParam(defaultValue = "0.5") double threshold) {
        List<Document> docs = knowledgeBaseService.search(query, topK, threshold);
        List<Map<String, Object>> results = docs.stream()
                .map(doc -> Map.<String, Object>of(
                        "content", doc.getText().substring(0, Math.min(200, doc.getText().length())) + "...",
                        "score", doc.getScore(),
                        "metadata", doc.getMetadata()
                ))
                .collect(Collectors.toList());
        return Map.of("query", query, "count", results.size(), "results", results);
    }

    /**
     * RAG 问答
     * GET http://localhost:9008/knowledge/ask?question=xxx
     */
    @GetMapping("/ask")
    public Map<String, Object> ask(@RequestParam String question) {
        String answer = ragChatService.ask(question);
        return Map.of("question", question, "answer", answer);
    }
}
