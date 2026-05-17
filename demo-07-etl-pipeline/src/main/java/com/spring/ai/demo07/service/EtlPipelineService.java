package com.spring.ai.demo07.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.document.DocumentWriter;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.writer.FileDocumentWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ETL 管道服务 — 演示文档的读取、拆分、写入完整流程
 */
@Service
public class EtlPipelineService {

    private final Resource markdownResource;

    public EtlPipelineService(
            @Value("classpath:documents/java-basics.md") Resource markdownResource) {
        this.markdownResource = markdownResource;
    }

    /**
     * 执行完整的 ETL 管道：读取 Markdown -> 拆分 -> 写入文件
     */
    public String executeFullPipeline() {
        // 第一步：Extract — 读取文档
        DocumentReader reader = createMarkdownReader();
        List<Document> documents = reader.read();
        System.out.println("读取到 " + documents.size() + " 个文档片段");

        // 第二步：Transform — 拆分文档
        DocumentTransformer splitter = createTextSplitter();
        List<Document> splitDocs = splitter.transform(documents);
        System.out.println("拆分后得到 " + splitDocs.size() + " 个文档块");

        // 第三步：Load — 写入文件（下一篇会换成写入向量数据库）
        DocumentWriter writer = createFileWriter();
        writer.accept(splitDocs);

        return String.format("ETL 完成：原始 %d 个片段 -> 拆分后 %d 个文档块",
                documents.size(), splitDocs.size());
    }

    /**
     * 仅读取文档，不做后续处理
     */
    public List<Document> readDocuments() {
        DocumentReader reader = createMarkdownReader();
        return reader.read();
    }

    /**
     * 仅拆分文档
     */
    public List<Document> splitDocuments(List<Document> documents) {
        DocumentTransformer splitter = createTextSplitter();
        return splitter.transform(documents);
    }

    /**
     * 创建 Markdown 文档读取器
     * 使用水平线（---）分割文档，不将代码块和引用块分离成独立文档
     */
    private DocumentReader createMarkdownReader() {
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)     // 遇到 --- 就分割成新文档
                .withIncludeCodeBlock(true)                 // 代码块保留在正文中
                .withIncludeBlockquote(true)                // 引用块保留在正文中
                .withAdditionalMetadata("source", "java-basics.md")
                .withAdditionalMetadata("course", "Java 基础教程")
                .build();

        return new MarkdownDocumentReader(markdownResource, config);
    }

    /**
     * 创建文本拆分器
     * 按照token数量拆分，每块最多500 token，重叠50 token
     */
    private DocumentTransformer createTextSplitter() {
        return TokenTextSplitter.builder()
                .withChunkSize(500)              // 每块最多500个token
                .withMinChunkSizeChars(100)      // 最小块大小100字符
                .withMinChunkLengthToEmbed(10)    // 低于10字符的块直接丢弃
                .withMaxNumChunks(100)            // 最多拆出100块
                .withKeepSeparator(true)          // 保留分隔符
                .build();
    }

    /**
     * 创建文件写入器
     */
    private DocumentWriter createFileWriter() {
        return new FileDocumentWriter("etl-output.txt", true);
    }
}
