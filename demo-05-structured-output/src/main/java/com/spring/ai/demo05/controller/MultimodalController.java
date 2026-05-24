package com.spring.ai.demo05.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.content.Media;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 多模态示例
 * 演示 Spring AI 的多模态输入能力：图片理解、图片 + 结构化输出组合
 *
 * 注意：DeepSeek 当前不支持多模态输入，以下代码展示 API 用法。
 * 如需实际测试，请切换到支持多模态的模型（如 OpenAI GPT-4o、Claude 等），
 * 只需更换 pom.xml 中的 Starter 即可，代码无需修改。
 */
@RestController
@RequestMapping("/multimodal")
class MultimodalController {

    private final ChatClient chatClient;

    public MultimodalController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个教育领域的智能助手，能够识别和分析教学相关的图片内容。")
                .build();
    }

    // ========== 1. URL 图片描述：最基础的用法 ==========

    /**
     * 通过 URL 分析图片内容
     * 示例：GET /multimodal/describe?url=https://example.com/photo.jpg
     */
    @GetMapping("/describe")
    String describeFromUrl(@RequestParam String url) {
        return chatClient.prompt()
                .user(u -> u.text("请详细描述这张图片的内容。如果是试题，请逐一分析每道题。")
                        .media(new Media(MimeTypeUtils.IMAGE_JPEG, java.net.URI.create(url))))
                .call()
                .content();
    }

    // ========== 2. 上传图片分析：处理用户上传的文件 ==========

    /**
     * 上传图片进行分析
     * 示例：POST /multimodal/upload，form-data: file=<图片文件>
     */
    @PostMapping("/upload")
    String uploadAndAnalyze(@RequestParam("file") MultipartFile file) throws Exception {
        var resource = file.getResource();
        var mimeType = org.springframework.util.MimeType.valueOf(file.getContentType());

        return chatClient.prompt()
                .user(u -> u.text("请分析这张图片的内容，如果包含题目请逐一解析。")
                        .media(mimeType, resource))
                .call()
                .content();
    }

    // ========== 3. 图片 + 结构化输出：从试卷图片提取题目 ==========

    /**
     * 上传试卷图片，提取为结构化的题目列表
     * 演示多模态 + 结构化输出的组合能力
     * 示例：POST /multimodal/extract-quiz，form-data: file=<试卷图片>
     */
    @PostMapping("/extract-quiz")
    ResponseEntity<?> extractQuizFromImage(@RequestParam("file") MultipartFile file) throws Exception {
        try {
            var resource = file.getResource();
            var mimeType = org.springframework.util.MimeType.valueOf(file.getContentType());

            List<ExtractedQuestion> questions = chatClient.prompt()
                    .user(u -> u.text("""
                            请识别这张试卷图片中的所有选择题，提取为结构化数据。
                            每道题包含：题目内容、四个选项、正确答案（如果能判断的话）和考点。
                            """)
                            .media(mimeType, resource))
                    .options(ChatOptions.builder()
                            .temperature(0.2)
                            .build())
                    .call()
                    .entity(new ParameterizedTypeReference<>() {});

            if (questions == null || questions.isEmpty()) {
                return ResponseEntity.ok("未能识别出选择题，请确保图片清晰且包含选择题。");
            }
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("图片解析失败: " + e.getMessage());
        }
    }

    // ========== 4. 图片评分：上传手写作文图片 ==========

    /**
     * 上传手写作文图片，AI 进行多维度评分
     * 演示多模态 + 复杂嵌套结构化输出
     * 示例：POST /multimodal/grade-essay，form-data: file=<作文图片>
     */
    @PostMapping("/grade-essay")
    ResponseEntity<?> gradeEssay(@RequestParam("file") MultipartFile file) throws Exception {
        try {
            var resource = file.getResource();
            var mimeType = org.springframework.util.MimeType.valueOf(file.getContentType());

            EssayGrade grade = chatClient.prompt()
                    .user(u -> u.text("""
                            请仔细阅读这张手写作文图片，按以下维度评分：
                            1. 内容与立意（主题是否明确、内容是否充实）
                            2. 结构与逻辑（段落划分、论证是否清晰）
                            3. 语言表达（用词、句式、修辞）
                            4. 书写规范（字迹是否清晰、有无错别字）
                            """)
                            .media(mimeType, resource))
                    .options(ChatOptions.builder()
                            .temperature(0.2)
                            .build())
                    .call()
                    .entity(EssayGrade.class);

            return ResponseEntity.ok(grade);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("作文评分失败: " + e.getMessage());
        }
    }

    // ========== 5. ClassPath 资源示例：本地图片分析 ==========

    /**
     * 使用 classpath 下的本地图片进行测试
     * 把测试图片放到 src/main/resources/images/ 目录下即可
     * 示例：GET /multimodal/classpath?filename=test.png
     */
    @GetMapping("/classpath")
    String analyzeClasspathImage(
            @RequestParam(defaultValue = "images/test.png") String filename) {
        return chatClient.prompt()
                .user(u -> u.text("请描述这张图片的内容。")
                        .media(MimeTypeUtils.IMAGE_PNG,
                                new org.springframework.core.io.ClassPathResource(filename)))
                .call()
                .content();
    }

    // ========== DTO 定义 ==========

    /** 从试卷图片提取的选择题 */
    record ExtractedQuestion(
            String question,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String correctAnswer,
            String knowledgePoint
    ) {}

    /** 作文评分结果 */
    record EssayGrade(
            int totalScore,
            String contentComment,
            String structureComment,
            String languageComment,
            String writingComment,
            String summary,
            List<String> suggestions
    ) {}
}
