package com.spring.ai.demo08.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识库服务：负责文档的加载和相似度搜索
 * 对接第 7 篇的 ETL 管道，将"写入文件"换成"写入向量数据库"
 */
@Service
public class KnowledgeBaseService {

    private final VectorStore vectorStore;
    private final Resource documentResource;

    public KnowledgeBaseService(VectorStore vectorStore,
                                @Value("classpath:documents/spring-ai-basics.md") Resource documentResource) {
        this.vectorStore = vectorStore;
        this.documentResource = documentResource;
    }

    /**
     * 加载文档到向量数据库
     * 流程：读取 Markdown -> 按 Token 拆分 -> 调用 Embedding 生成向量 -> 存储
     */
    public int loadDocuments() {
        // 第一步：读取 Markdown 文档
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)     // 遇到 --- 分割
                .withIncludeCodeBlock(true)                 // 保留代码块
                .withIncludeBlockquote(true)                // 保留引用块
                .withAdditionalMetadata("source", "spring-ai-basics.md")
                .withAdditionalMetadata("category", "Spring AI 教程")
                .build();
        MarkdownDocumentReader reader = new MarkdownDocumentReader(documentResource, config);
        List<Document> documents = reader.read();

        // 第二步：按 Token 拆分文档
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(500)              // 每块最多 500 个 token
                .withMinChunkSizeChars(100)      // 最小块 100 字符
                .withMinChunkLengthToEmbed(10)   // 低于 10 字符的块丢弃
                .withMaxNumChunks(200)           // 最多拆出 200 块
                .withKeepSeparator(true)         // 保留分隔符
                .build();
        List<Document> splitDocs = splitter.transform(documents);

        // 第三步：写入向量数据库
        // vectorStore.add() 内部会自动调用 EmbeddingModel 生成向量
        vectorStore.add(splitDocs);

        return splitDocs.size();
    }

    /**
     * 相似度搜索
     *
     * @param query     查询文本
     * @param topK      返回最多 topK 条结果
     * @param threshold 相似度阈值（0-1），低于此值的结果被过滤
     * @return 匹配的文档列表
     */
    public List<Document> search(String query, int topK, double threshold) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(threshold)
                .build();
        return vectorStore.similaritySearch(request);
    }
}
