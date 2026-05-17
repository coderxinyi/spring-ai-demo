package com.spring.ai.demo02.scenario;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 多角色 AI 助手场景：展示 Spring AI 如何用同一个 ChatModel
 * 创建不同角色的 AI 助手
 */
@RestController
class MultiRoleAssistantController {

    // 数学老师
    private final ChatClient mathTeacher;
    // 英语作文批改助手
    private final ChatClient essayReviewer;

    public MultiRoleAssistantController(ChatClient.Builder builder) {
        // 数学老师：解题思路导向，引导式教学
        this.mathTeacher = builder
                .defaultSystem("你是一位经验丰富的数学老师。" +
                        "当学生问数学题时，不要直接给答案。" +
                        "先分析题目涉及的知识点，然后一步步引导学生思考，" +
                        "每一步都问学生'你觉得下一步该怎么做？'。" +
                        "用生活中的例子帮助理解抽象概念。")
                .build();

        // 英语作文批改助手：逐句点评 + 修改建议
        this.essayReviewer = builder
                .defaultSystem("你是一位专业的英语作文批改老师。" +
                        "学生提交英语作文后，你需要：" +
                        "1. 逐段给出总体评价（优秀/良好/需改进）" +
                        "2. 指出语法错误并给出正确写法" +
                        "3. 标注用词不当的地方，推荐更地道的表达" +
                        "4. 给出整体评分（满分100）和改进建议。" +
                        "语气要鼓励为主，不要打击学生的信心。")
                .build();
    }

    /**
     * 数学老师答疑
     * 示例：GET /math?question=一元二次方程怎么解
     */
    @GetMapping("/math")
    String mathQuestion(@RequestParam String question) {
        return mathTeacher.prompt()
                .user(question)
                .call()
                .content();
    }

    /**
     * 英语作文批改
     * 示例：GET /essay?text=Yesterday I go to school...
     */
    @GetMapping("/essay")
    String essayReview(@RequestParam String text) {
        return essayReviewer.prompt()
                .user("请批改以下英语作文：\n\n" + text)
                .call()
                .content();
    }
}
