package com.spring.ai.demo07.controller;

import com.spring.ai.demo07.service.EtlPipelineService;
import com.spring.ai.demo07.service.MetadataEnricherService;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ETL 管道演示控制器
 */
@RestController
@RequestMapping("/etl")
public class EtlPipelineController {

    private final EtlPipelineService etlPipelineService;
    private final MetadataEnricherService metadataEnricherService;

    public EtlPipelineController(EtlPipelineService etlPipelineService,
                                  MetadataEnricherService metadataEnricherService) {
        this.etlPipelineService = etlPipelineService;
        this.metadataEnricherService = metadataEnricherService;
    }

    /**
     * 执行完整的 ETL 管道
     */
    @GetMapping("/full-pipeline")
    public Map<String, Object> fullPipeline() {
        String result = etlPipelineService.executeFullPipeline();
        return Map.of(
                "status", "success",
                "message", result
        );
    }

    /**
     * 演示第一步：读取文档（Extract）
     */
    @GetMapping("/read")
    public Map<String, Object> readDocuments() {
        List<Document> documents = etlPipelineService.readDocuments();
        List<Map<String, Object>> docSummaries = documents.stream()
                .map(doc -> Map.<String, Object>of(
                        "content", doc.getText().substring(0, Math.min(100, doc.getText().length())) + "...",
                        "metadata", doc.getMetadata()
                ))
                .collect(Collectors.toList());

        return Map.of(
                "count", documents.size(),
                "documents", docSummaries
        );
    }

    /**
     * 演示第二步：拆分文档（Transform）
     */
    @GetMapping("/split")
    public Map<String, Object> splitDocuments() {
        List<Document> rawDocs = etlPipelineService.readDocuments();
        List<Document> splitDocs = etlPipelineService.splitDocuments(rawDocs);

        List<Map<String, Object>> chunkSummaries = splitDocs.stream()
                .map(doc -> Map.<String, Object>of(
                        "content", doc.getText().substring(0, Math.min(80, doc.getText().length())) + "...",
                        "charCount", doc.getText().length()
                ))
                .collect(Collectors.toList());

        return Map.of(
                "rawCount", rawDocs.size(),
                "splitCount", splitDocs.size(),
                "chunks", chunkSummaries
        );
    }

    /**
     * 演示关键词元数据增强（用 AI 提取关键词）
     */
    @GetMapping("/enrich")
    public Map<String, Object> enrichDocuments() {
        List<Document> rawDocs = etlPipelineService.readDocuments();
        List<Document> enrichedDocs = metadataEnricherService.enrichWithKeywords(rawDocs);

        List<Map<String, Object>> results = enrichedDocs.stream()
                .map(doc -> {
                    String keywords = (String) doc.getMetadata().get("excerpt_keywords");
                    String preview = doc.getText().substring(0, Math.min(80, doc.getText().length())) + "...";
                    return Map.<String, Object>of(
                            "content", preview,
                            "keywords", keywords != null ? keywords : "无"
                    );
                })
                .collect(Collectors.toList());

        return Map.of(
                "count", enrichedDocs.size(),
                "documents", results
        );
    }
}
