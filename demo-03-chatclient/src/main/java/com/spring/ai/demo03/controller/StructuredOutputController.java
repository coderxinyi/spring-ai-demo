package com.spring.ai.demo03.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 结构化输出：让 AI 返回 Java 对象而不是字符串
 * 对应文章「二、怎么用」的结构化输出部分
 */
@RestController
class StructuredOutputController {

    private final ChatClient chatClient;

    public StructuredOutputController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个出题助手，根据学生的要求生成练习题。")
                .build();
    }

    /**
     * 单个对象的结构化输出
     * 示例：GET /struct/quiz?topic=一元一次方程
     */
    @GetMapping("/struct/quiz")
    QuizQuestion quiz(@RequestParam(defaultValue = "一元一次方程") String topic) {
        return chatClient.prompt()
                .user(u -> u.text("出一道关于{topic}的初中数学选择题，" +
                        "包含题目、四个选项和正确答案。")
                        .param("topic", topic))
                .call()
                .entity(QuizQuestion.class);
    }

    /**
     * 列表类型的结构化输出
     * 示例：GET /struct/quizzes?topic=英语语法&count=3
     */
    @GetMapping("/struct/quizzes")
    List<QuizQuestion> quizzes(
            @RequestParam(defaultValue = "英语语法") String topic,
            @RequestParam(defaultValue = "3") int count) {
        return chatClient.prompt()
                .user(u -> u.text("出{count}道关于{topic}的选择题")
                        .param("count", String.valueOf(count))
                        .param("topic", topic))
                .call()
                .entity(new ParameterizedTypeReference<>() {});
    }

    /**
     * 学习计划的结构化输出
     * 示例：GET /struct/plan?subject=数学&grade=初一
     */
    @GetMapping("/struct/plan")
    StudyPlan studyPlan(
            @RequestParam(defaultValue = "数学") String subject,
            @RequestParam(defaultValue = "初一") String grade) {
        return chatClient.prompt()
                .user(u -> u.text("为{grade}{subject}学生制定一周的学习计划，" +
                        "包含每天的学习内容和目标。")
                        .param("grade", grade)
                        .param("subject", subject))
                .call()
                .entity(StudyPlan.class);
    }

    // ---- 结构化输出的 DTO 定义 ----

    record QuizQuestion(
            String question,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String correctAnswer,
            String explanation
    ) {}

    record StudyPlan(
            String subject,
            String grade,
            List<DailyTask> dailyTasks
    ) {}

    record DailyTask(
            String day,
            String topic,
            String goal,
            List<String> activities
    ) {}
}
