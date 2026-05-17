package com.spring.ai.demo10;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.RelevancyEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StudentAssistantEvaluationTest {

    @Autowired
    private ChatModel chatModel;

    @Test
    void testRelevancyEvaluation() {
        // 模拟一个 RAG 场景的评估
        String userQuestion = "张三的数据结构成绩是多少？";
        String context = "张三的成绩：高等数学 92 分，英语精读 85 分，数据结构 88 分";
        String aiResponse = "张三的数据结构成绩是 88 分";

        // 构建评估请求
        EvaluationRequest request = new EvaluationRequest(
                userQuestion,
                List.of(new SystemMessage(context)),
                aiResponse
        );

        // 用 RelevancyEvaluator 评估相关性
        RelevancyEvaluator evaluator = new RelevancyEvaluator(ChatClient.builder(chatModel));
        EvaluationResponse response = evaluator.evaluate(request);

        // 断言：AI 认为回答与问题相关
        assertThat(response.isPass()).isTrue();
    }
}
