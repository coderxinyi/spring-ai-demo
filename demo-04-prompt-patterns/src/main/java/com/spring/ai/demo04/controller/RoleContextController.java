package com.spring.ai.demo04.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模式 3-5：System Prompting、Role Prompting、Contextual Prompting
 * 框架类模式，用于控制 AI 的行为框架
 */
@RestController
@RequestMapping("/prompt")
class RoleContextController {

    private final ChatClient systemChatClient;
    private final ChatClient mathTeacher;
    private final ChatClient studyPlanner;

    public RoleContextController(ChatClient.Builder builder) {
        // 模式 3 用的通用 ChatClient
        this.systemChatClient = builder
                .defaultSystem("你是一个教育领域的智能助手。")
                .build();

        // 模式 4：数学老师 —— 引导式教学，不直接给答案
        this.mathTeacher = builder
                .defaultSystem("""
                        你是一位有 20 年教学经验的数学老师。
                        当学生问你数学题时：
                        1. 不要直接给答案
                        2. 先分析题目涉及的知识点
                        3. 用生活中的例子帮助理解
                        4. 一步步引导学生思考
                        """)
                .build();

        // 模式 4：学习规划师 —— 制定具体可执行的计划
        this.studyPlanner = builder
                .defaultSystem("""
                        你是一位学习规划师，专门帮助中学生制定学习计划。
                        根据学生的年级、目标和时间安排，制定具体的日计划。
                        """)
                .build();
    }

    // ========== 模式 3：System Prompting（系统提示） ==========

    /**
     * 通过 System Prompt 约束输出格式 + 结构化输出
     * 适用场景：需要全局规则约束、统一输出格式
     */
    @GetMapping("/system-prompt")
    QuizResult systemPrompt(
            @RequestParam(defaultValue = "勾股定理") String topic) {
        return systemChatClient.prompt()
                .system("""
                        你是一个自动出题系统。根据学生输入的知识点，生成一道选择题。
                        返回 JSON 格式，包含题目、四个选项、正确答案和解析。
                        """)
                .user(u -> u.text("请出一道关于 {topic} 的中学数学选择题")
                        .param("topic", topic))
                .options(ChatOptions.builder()
                        .temperature(0.3)
                        .maxTokens(500)
                        .build())
                .call()
                .entity(QuizResult.class);
    }

    record QuizResult(
            String question,
            String optionA, String optionB, String optionC, String optionD,
            String correctAnswer,
            String explanation
    ) {}

    // ========== 模式 4：Role Prompting（角色提示） ==========

    /**
     * 数学老师角色 —— 引导式教学
     */
    @GetMapping("/role/math")
    String roleMath(@RequestParam String question) {
        return mathTeacher.prompt()
                .user(question)
                .call()
                .content();
    }

    /**
     * 学习规划师角色 —— 制定学习计划
     */
    @GetMapping("/role/plan")
    String rolePlan(
            @RequestParam(defaultValue = "初二") String grade,
            @RequestParam(defaultValue = "期末复习") String goal) {
        return studyPlanner.prompt()
                .user(u -> u.text("我是{grade}学生，目标是{goal}，请制定两周的学习计划。")
                        .param("grade", grade)
                        .param("goal", goal))
                .call()
                .content();
    }

    // ========== 模式 5：Contextual Prompting（上下文提示） ==========

    /**
     * 注入学生画像上下文，做个性化课程推荐
     * 适用场景：需要根据用户信息做个性化响应
     */
    @GetMapping("/contextual")
    String contextual(
            @RequestParam(defaultValue = "推荐几门适合我的课") String question,
            @RequestParam(defaultValue = "初二学生，数学成绩一般，喜欢动手实验") String context) {
        return systemChatClient.prompt()
                .user(u -> u.text("""
                        {question}

                        学生画像：{context}
                        """)
                        .param("question", question)
                        .param("context", context))
                .call()
                .content();
    }
}
