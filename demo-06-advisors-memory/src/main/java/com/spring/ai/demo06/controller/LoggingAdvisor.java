package com.spring.ai.demo06.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

/**
 * 自定义日志 Advisor
 * 演示如何实现 CallAdvisor 接口，在请求前后打印日志
 */
public class LoggingAdvisor implements CallAdvisor {

    private static final Logger log = LoggerFactory.getLogger(LoggingAdvisor.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        // 数字越小优先级越高，越先执行
        return 0;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        // 前置处理：记录用户请求
        String userText = request.prompt().getUserMessage().getText();
        log.info("[LoggingAdvisor] 收到用户消息: {}", userText);

        long startTime = System.currentTimeMillis();

        // 调用下一个 Advisor（责任链模式）
        ChatClientResponse response = chain.nextCall(request);

        // 后置处理：记录 AI 响应和耗时
        long elapsed = System.currentTimeMillis() - startTime;
        String aiText = response.chatResponse().getResult().getOutput().getText();
        log.info("[LoggingAdvisor] AI 响应 (耗时{}ms): {}", elapsed,
                aiText.length() > 100 ? aiText.substring(0, 100) + "..." : aiText);

        return response;
    }
}
