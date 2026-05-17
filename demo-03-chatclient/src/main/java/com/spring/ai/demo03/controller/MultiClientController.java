package com.spring.ai.demo03.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 多角色助手：用同一个 Builder 创建不同角色的 ChatClient
 * 对应文章「二、怎么用」的多角色场景部分
 */
@RestController
class MultiClientController {

    // 数学老师：引导式教学
    private final ChatClient mathTeacher;
    // 作文批改老师
    private final ChatClient essayReviewer;
    // 学习规划师
    private final ChatClient studyPlanner;

    public MultiClientController(ChatClient.Builder builder) {
        this.mathTeacher = builder
                .defaultSystem("你是一位经验丰富的数学老师。" +
                        "当学生问数学题时，不要直接给答案。" +
                        "先分析题目涉及的知识点，然后一步步引导学生思考。" +
                        "用生活中的例子帮助理解抽象概念。")
                .build();

        this.essayReviewer = builder
                .defaultSystem("你是一位专业的作文批改老师。" +
                        "学生提交作文后，你需要：" +
                        "1. 给出总体评价（优秀/良好/需改进）" +
                        "2. 指出具体的语法和用词问题" +
                        "3. 给出改进建议和范文片段" +
                        "语气要鼓励为主，不要打击学生的信心。")
                .build();

        this.studyPlanner = builder
                .defaultSystem("你是一位学习规划师。" +
                        "根据学生的年级、目标和时间安排，制定合理的学习计划。" +
                        "计划要具体到每天的任务，并给出学习方法的建议。")
                .build();
    }

    /**
     * 数学老师答疑
     * 示例：GET /multi/math?question=一元二次方程怎么解
     */
    @GetMapping("/multi/math")
    String math(@RequestParam String question) {
        return mathTeacher.prompt()
                .user(question)
                .call()
                .content();
    }

    /**
     * 作文批改
     * 示例：GET /multi/essay?text=春天来了，花开了...
     */
    @GetMapping("/multi/essay")
    String essay(@RequestParam String text) {
        return essayReviewer.prompt()
                .user("请批改以下作文：\n\n" + text)
                .call()
                .content();
    }

    /**
     * 学习规划
     * 示例：GET /multi/plan?grade=初二&goal=期末考试复习
     */
    @GetMapping("/multi/plan")
    String plan(
            @RequestParam(defaultValue = "初二") String grade,
            @RequestParam(defaultValue = "期末考试复习") String goal) {
        return studyPlanner.prompt()
                .user(u -> u.text("我是{grade}学生，目标是{goal}，" +
                        "请帮我制定两周的学习计划。")
                        .param("grade", grade)
                        .param("goal", goal))
                .call()
                .content();
    }
}
