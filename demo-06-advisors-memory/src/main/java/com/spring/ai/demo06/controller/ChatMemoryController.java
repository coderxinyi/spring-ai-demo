package com.spring.ai.demo06.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 对话记忆示例
 * 演示 Advisors API 和 ChatMemory 的各种用法
 */
@RestController
@RequestMapping("/chat")
class ChatMemoryController {

    private static final Logger log = LoggerFactory.getLogger(ChatMemoryController.class);

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ChatModel chatModel;

    public ChatMemoryController(ChatClient.Builder chatClientBuilder, ChatModel chatModel) {
        // 1. 创建内存存储（生产环境替换为 JDBC/Cassandra 等）
        ChatMemoryRepository repository = new InMemoryChatMemoryRepository();

        // 2. 创建消息窗口记忆，保留最近 20 条消息
        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(20)
                .build();

        // 3. 构建 ChatClient，注册 MessageChatMemoryAdvisor
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个教育培训行业的AI学习助手，名叫小智。你可以回答学科问题、推荐学习资源、制定学习计划。回答要简洁、友好。")
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();

        this.chatModel = chatModel;
    }

    // ========== 1. 基础多轮对话：AI 能记住上下文 ==========

    /**
     * 带记忆的多轮对话
     * 同一个 conversationId 下的对话会共享上下文
     * 示例：
     *   GET /chat/talk?conversationId=stu001&message=我叫小明，我是初二学生
     *   GET /chat/talk?conversationId=stu001&message=我叫什么名字？
     *   GET /chat/talk?conversationId=stu001&message=帮我推荐适合我年级的数学学习资料
     */
    @GetMapping("/talk")
    String talk(
            @RequestParam String conversationId,
            @RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    // ========== 2. 不同会话隔离：不同 conversationId 互不干扰 ==========

    /**
     * 不同会话隔离
     * stu001 和 stu002 的对话互不影响
     * 示例：
     *   GET /chat/session?conversationId=stu001&message=我最喜欢的科目是物理
     *   GET /chat/session?conversationId=stu002&message=我最喜欢的科目是英语
     *   GET /chat/session?conversationId=stu001&message=我最喜欢什么科目？  → 物理
     *   GET /chat/session?conversationId=stu002&message=我最喜欢什么科目？  → 英语
     */
    @GetMapping("/session")
    String session(
            @RequestParam String conversationId,
            @RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    // ========== 3. 清除会话记忆 ==========

    /**
     * 清除某个会话的记忆
     * 示例：GET /chat/clear?conversationId=stu001
     */
    @GetMapping("/clear")
    ResponseEntity<String> clear(@RequestParam String conversationId) {
        chatMemory.clear(conversationId);
        return ResponseEntity.ok("会话 " + conversationId + " 的记忆已清除");
    }

    // ========== 4. 流式对话记忆 ==========

    /**
     * 流式对话（也支持记忆）
     * 示例：GET /chat/stream?conversationId=stu001&message=帮我分析一下勾股定理
     */
    @GetMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    Flux<String> stream(
            @RequestParam String conversationId,
            @RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }

    // ========== 5. 带自定义日志 Advisor 的对话 ==========

    /**
     * 带自定义日志 Advisor 的对话
     * 可以在控制台看到请求和响应的日志输出
     * 示例：GET /chat/log?conversationId=stu001&message=什么是牛顿第三定律？
     */
    @GetMapping("/log")
    String talkWithLogging(
            @RequestParam String conversationId,
            @RequestParam String message) {

        // 创建一个临时 ChatClient，额外注册日志 Advisor
        ChatClient loggingClient = ChatClient.builder(chatModel)
                .defaultSystem("你是一个教育培训行业的AI学习助手，名叫小智。")
                .defaultAdvisors(
                        new LoggingAdvisor(),  // 自定义日志 Advisor（先执行）
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();

        return loggingClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}
