package com.spring.ai.demo04.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.options.ChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 模式 6-9：Step-Back、Chain of Thought、Self-Consistency、Tree of Thoughts
 * 推理类模式，让 AI 的思考过程更可追溯、更可靠
 */
@RestController
@RequestMapping("/prompt")
class ReasoningController {

    private final ChatClient chatClient;

    public ReasoningController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个教育领域的智能助手。")
                .build();
    }

    // ========== 模式 6：Step-Back（退步提示） ==========

    /**
     * 先获取基础原理，再基于原理回答具体问题
     * 适用场景：需要领域知识的复杂任务
     */
    @GetMapping("/step-back")
    String stepBack(
            @RequestParam(defaultValue = "初中物理") String subject) {
        // 第一步：退一步，获取相关的基础教学原则
        String principles = chatClient.prompt()
                .user(u -> u.text("""
                        关于 {subject}，列出 5 个核心教学原则，
                        这些原则有助于让课程设计既有趣又有深度。
                        """).param("subject", subject))
                .call()
                .content();

        // 第二步：基于背景知识设计具体方案
        return chatClient.prompt()
                .user(u -> u.text("""
                        请基于以下教学原则，设计一份为期一周的 {subject} 课程大纲：
                        {principles}

                        要求每天有明确的教学目标和活动安排。
                        """)
                        .param("subject", subject)
                        .param("principles", principles))
                .call()
                .content();
    }

    // ========== 模式 7：Chain of Thought（思维链） ==========

    /**
     * Zero-Shot CoT：让 AI 一步步推理数学题
     * 适用场景：数学推理、逻辑分析等需要展示过程的任务
     */
    @GetMapping("/cot")
    String chainOfThought(
            @RequestParam(defaultValue = "小明有15个苹果，给了小红1/3，又给了小华剩下的1/2，小明还剩几个苹果？") String problem) {
        return chatClient.prompt()
                .user(u -> u.text("""
                        请解答以下数学题：

                        {problem}

                        请一步一步思考，展示完整的计算过程。
                        """).param("problem", problem))
                .options(ChatOptions.builder()
                        .temperature(0.3)
                        .maxTokens(500)
                        .build())
                .call()
                .content();
    }

    /**
     * Few-Shot CoT：给出推理范例，让 AI 照着推理
     * 适用场景：需要特定推理格式的复杂逻辑题
     */
    @GetMapping("/cot/few-shot")
    String cotFewShot(@RequestParam String problem) {
        return chatClient.prompt()
                .user(u -> u.text("""
                        以下是数学题的解答示例：

                        题目：一个水池有两个进水管，A管单独注满需要6小时，B管单独注满需要4小时。两管同时开，几小时注满？
                        解答：
                        1) A管每小时注入水池的 1/6
                        2) B管每小时注入水池的 1/4
                        3) 两管同时开，每小时注入 1/6 + 1/4 = 5/12
                        4) 注满时间 = 1 ÷ (5/12) = 12/5 = 2.4 小时
                        答案：2.4小时

                        现在请解答：
                        {problem}
                        """).param("problem", problem))
                .call()
                .content();
    }

    // ========== 模式 8：Self-Consistency（自洽性投票） ==========

    /**
     * 同一个问题跑多次，投票取多数
     * 适用场景：高准确率要求的分类和判断任务
     * 注意：会调用 AI 5 次，延迟和成本较高
     */
    @GetMapping("/self-consistency")
    Map<String, Object> selfConsistency(
            @RequestParam(defaultValue = "学生的作文里出现了大量口语化表达，但论点清晰，论据充分，结构完整。请判断这篇作文的等级：A/B/C") String question) {

        // 统计投票结果
        Map<String, Integer> votes = new HashMap<>();

        // 运行 5 次，每次用较高温度产生不同的推理路径
        for (int i = 0; i < 5; i++) {
            GradeResult result = chatClient.prompt()
                    .user(u -> u.text("""
                            {question}

                            请一步步分析，然后给出等级判断。
                            """).param("question", question))
                    .options(ChatOptions.builder()
                            .temperature(0.8)  // 较高温度，让每次推理有差异
                            .build())
                    .call()
                    .entity(GradeResult.class);

            // 累计投票
            votes.merge(result.grade(), 1, Integer::sum);
        }

        // 找出票数最多的结果
        String finalGrade = votes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("B");

        return Map.of("votes", votes, "finalGrade", finalGrade);
    }

    record GradeResult(String grade, String reasoning) {}

    // ========== 模式 9：Tree of Thoughts（思维树） ==========

    /**
     * 探索多条推理路径，评估后选最优
     * 适用场景：战略规划、方案选型等需要比较多种可能性的任务
     */
    @GetMapping("/tot")
    String treeOfThoughts(
            @RequestParam(defaultValue = "初三学生") String studentProfile) {
        // 第一步：生成多个备选方案
        String options = chatClient.prompt()
                .user(u -> u.text("""
                        面对一个 {profile} 的中考冲刺规划，请生成 3 种不同的学习策略：
                        1. 策略一：侧重基础巩固型
                        2. 策略二：侧重弱项突破型
                        3. 策略三：侧重真题模拟型

                        对每种策略，说明核心理念、适合的学生类型、以及预期效果。
                        """).param("profile", studentProfile))
                .options(ChatOptions.builder()
                        .temperature(0.7)
                        .build())
                .call()
                .content();

        // 第二步：评估各方案，选出最优
        String evaluation = chatClient.prompt()
                .user(u -> u.text("""
                        以下是三种中考冲刺策略：
                        {options}

                        请从以下维度评估每种策略：
                        1. 可执行性（1-10分）
                        2. 见效速度（1-10分）
                        3. 可持续性（1-10分）

                        然后选出综合最优的策略，说明理由。
                        """).param("options", options))
                .call()
                .content();

        // 第三步：基于最优策略细化执行方案
        return chatClient.prompt()
                .user(u -> u.text("""
                        基于以下策略评估：
                        {evaluation}

                        请针对选出的最优策略，制定一个具体的 30 天执行计划，
                        包括每天的学习任务和时间安排。
                        """).param("evaluation", evaluation))
                .call()
                .content();
    }
}
