package com.spring.ai.demo05.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 结构化输出示例
 * 演示 entity() 的各种用法：单对象、列表、嵌套对象、Map、List
 */
@RestController
@RequestMapping("/struct")
class StructuredOutputController {

    private final ChatClient chatClient;

    public StructuredOutputController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个教育领域的智能助手，帮助分析和生成教学内容。")
                .build();
    }

    // ========== 1. 单对象映射：最基本的用法 ==========

    /**
     * 生成一道选择题，直接映射为 QuizQuestion 对象
     * 示例：GET /struct/quiz?topic=一元一次方程
     */
    @GetMapping("/quiz")
    QuizQuestion quiz(@RequestParam(defaultValue = "一元一次方程") String topic) {
        return chatClient.prompt()
                .user(u -> u.text("出一道关于{topic}的初中选择题，包含题目、四个选项、正确答案和解析。")
                        .param("topic", topic))
                .options(ChatOptions.builder()
                        .temperature(0.3)  // 结构化输出用低温度，提高确定性
                        .build())
                .call()
                .entity(QuizQuestion.class);
    }

    // ========== 2. 列表映射：泛型类型用 ParameterizedTypeReference ==========

    /**
     * 批量生成题目，返回 List<QuizQuestion>
     * 示例：GET /struct/quiz-batch?topic=英语语法&count=3
     */
    @GetMapping("/quiz-batch")
    List<QuizQuestion> quizBatch(
            @RequestParam(defaultValue = "英语语法") String topic,
            @RequestParam(defaultValue = "3") int count) {
        return chatClient.prompt()
                .user(u -> u.text("出{count}道关于{topic}的初中选择题，每题包含题目、四个选项、正确答案和解析。")
                        .param("count", String.valueOf(count))
                        .param("topic", topic))
                .options(ChatOptions.builder()
                        .temperature(0.3)
                        .build())
                .call()
                .entity(new ParameterizedTypeReference<>() {});
    }

    // ========== 3. 嵌套对象：复杂业务结构 ==========

    /**
     * 生成学习诊断报告，包含嵌套的科目分析
     * 示例：GET /struct/report?name=小明
     */
    @GetMapping("/report")
    StudyReport report(@RequestParam(defaultValue = "小明") String name) {
        return chatClient.prompt()
                .user(u -> u.text("""
                        学生{student}最近一次考试：数学85分，英语72分，物理91分，化学68分。
                        请生成一份学习诊断报告，分析各科表现并给出建议。
                        """)
                        .param("student", name))
                .options(ChatOptions.builder()
                        .temperature(0.3)
                        .build())
                .call()
                .entity(StudyReport.class);
    }

    // ========== 4. Map 输出：不需要预定义类 ==========

    /**
     * 解析课程信息为 Map
     * 示例：GET /struct/schedule?text=周一上午8点数学在301教室，10点英语在205教室
     */
    @GetMapping("/schedule")
    Map<String, Object> schedule(
            @RequestParam(defaultValue = "周一上午8点数学在301教室，10点英语在205教室") String text) {
        return chatClient.prompt()
                .user(u -> u.text("将以下课程信息解析为结构化的 JSON：{text}")
                        .param("text", text))
                .options(ChatOptions.builder()
                        .temperature(0.1)
                        .build())
                .call()
                .entity(new ParameterizedTypeReference<>() {});
    }

    // ========== 5. List 输出：提取知识点列表 ==========

    /**
     * 提取知识点列表
     * 示例：GET /struct/topics?keyword=光合作用
     */
    @GetMapping("/topics")
    List<String> topics(@RequestParam(defaultValue = "光合作用") String keyword) {
        return chatClient.prompt()
                .user(u -> u.text("列出与\"{keyword}\"相关的5个核心知识点，每点一句话概括。")
                        .param("keyword", keyword))
                .options(ChatOptions.builder()
                        .temperature(0.3)
                        .build())
                .call()
                .entity(new ParameterizedTypeReference<>() {});
    }

    // ========== 6. 错误处理：生产环境必备 ==========

    /**
     * 带错误处理的结构化输出
     * 示例：GET /struct/quiz-safe?topic=勾股定理
     */
    @GetMapping("/quiz-safe")
    ResponseEntity<?> quizSafe(@RequestParam(defaultValue = "勾股定理") String topic) {
        try {
            QuizQuestion quiz = chatClient.prompt()
                    .user(u -> u.text("出一道关于{topic}的初中选择题")
                            .param("topic", topic))
                    .options(ChatOptions.builder()
                            .temperature(0.2)
                            .build())
                    .call()
                    .entity(QuizQuestion.class);

            // 校验关键字段是否存在
            if (quiz.correctAnswer() == null || quiz.question() == null) {
                return ResponseEntity.badRequest().body("AI 输出不完整，请重试");
            }
            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            // 捕获 JSON 解析失败等异常
            return ResponseEntity.internalServerError()
                    .body("结构化解析失败: " + e.getMessage());
        }
    }

    // ========== DTO 定义 ==========

    /** 单道选择题 */
    record QuizQuestion(
            String question,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String correctAnswer,
            String explanation
    ) {}

    /** 学习诊断报告（嵌套结构） */
    record StudyReport(
            String studentName,
            int overallScore,
            String summary,
            List<SubjectAnalysis> subjectAnalyses
    ) {}

    /** 单科分析 */
    record SubjectAnalysis(
            String subject,
            int score,
            String level,
            List<String> strengths,
            List<String> improvements
    ) {}
}
