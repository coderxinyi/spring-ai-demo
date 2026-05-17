# Spring AI 基础教程

## 什么是 Spring AI

Spring AI 是 Spring 生态中用于构建 AI 应用的框架。它提供了一套统一的 API 抽象层，让开发者可以用熟悉的方式（依赖注入、自动配置、Properties 配置）来接入各种 AI 模型和向量数据库，而不需要学习每个厂商的 SDK。

Spring AI 的核心理念是"可移植性"——同一套业务代码，切换底层模型只需要改配置文件，不需要改 Java 代码。

---

## 核心概念

### ChatModel

ChatModel 是 Spring AI 中最核心的接口，负责与大语言模型（LLM）进行对话交互。它接收用户消息，返回模型的回复。

Spring AI 支持多种 ChatModel 实现：OpenAI、DeepSeek、Ollama、Azure OpenAI 等。通过 Spring Boot 的自动配置机制，你只需要引入对应的 Starter 依赖，就能自动注入 ChatModel Bean。

使用 ChatModel 有两种方式：直接调用 ChatModel 接口，或者使用更高级的 ChatClient API。

### ChatClient

ChatClient 是 Spring AI 提供的流畅 API，封装了 ChatModel 的调用细节。它支持链式调用、Prompt 模板、Advisor 拦截器等功能。

ChatClient 的典型用法：通过 builder 模式创建实例，使用 prompt() 方法设置用户消息，通过 advisors() 添加拦截器，最后通过 call() 同步调用或 stream() 流式调用获取结果。

### Embedding Model

Embedding Model（嵌入模型）负责将文本转换为数值向量。向量是一段浮点数数组，比如 [0.12, -0.34, 0.56, ...]，通常有几百到几千个维度。

向量的核心特性是：语义相近的文本，转换后的向量在数学空间中也相近。这使得"语义搜索"成为可能——把用户问题和文档都转成向量，然后用数学方法（如余弦相似度）计算距离，就能找到语义最相关的文档。

Spring AI 提供了统一的 EmbeddingModel 接口，支持 OpenAI、Transformers (ONNX)、Ollama 等多种实现。

### VectorStore

VectorStore 是 Spring AI 对向量数据库的统一抽象。它提供两个核心能力：

1. **写入**：接收 Document 对象，自动调用 EmbeddingModel 生成向量并存储
2. **搜索**：接收查询文本，生成查询向量，在数据库中找到最相似的文档

Spring AI 支持 20 多种 VectorStore 实现，包括 PGVector（PostgreSQL）、Milvus、Redis、Chroma、Elasticsearch、Pinecone 等。还有一个 SimpleVectorStore 用于内存存储和测试。

---

## RAG（检索增强生成）

RAG 是 Retrieval-Augmented Generation 的缩写，即"检索增强生成"。它解决的核心问题是：大模型的知识截止到训练数据的时间点，无法回答私有数据或最新信息相关的问题。

RAG 的流程分三步：

1. **检索**：把用户的问题转换为向量，在向量数据库中搜索语义相关的文档片段
2. **增强**：把搜索到的文档作为上下文，和用户问题一起拼成一个增强的 Prompt
3. **生成**：将增强后的 Prompt 发给大模型，模型基于上下文生成回答

Spring AI 提供了 QuestionAnswerAdvisor 来简化 RAG 的实现。你只需要配置好 VectorStore，就能用几行代码实现完整的 RAG 问答。

---

## ETL 管道

ETL 管道是构建 RAG 应用的重要前置步骤。ETL 代表 Extract（抽取）、Transform（转换）、Load（加载）：

- **Extract**：从 PDF、Markdown、Word、HTML 等格式中读取文档内容
- **Transform**：将长文档拆分为合适大小的片段（Chunking），并添加元数据
- **Load**：将处理后的文档写入向量数据库

Spring AI 提供了 DocumentReader、DocumentTransformer、DocumentWriter 三个接口来抽象 ETL 的三个阶段。常见组合是 MarkdownDocumentReader + TokenTextSplitter + VectorStore。

---

## Advisor 机制

Advisor 是 Spring AI 的拦截器机制，类似 Spring MVC 的 Interceptor 或 Servlet Filter。它可以在请求发送给模型之前（before）和收到响应之后（after）插入自定义逻辑。

常见的 Advisor 应用场景：

- **对话记忆**：ChatMemoryAdvisor，在上下文中维护对话历史
- **RAG 检索**：QuestionAnswerAdvisor，自动从向量数据库检索相关文档
- **日志监控**：记录每次请求的耗时和 Token 消耗

Advisor 通过 ChatClient 的 advisors() 方法注册，支持链式组合多个 Advisor。

---

## Spring AI 与 LangChain4j 的对比

Spring AI 和 LangChain4j 都是 Java 生态中的 AI 应用开发框架，但设计理念有所不同。

Spring AI 深度融入 Spring 生态，使用 Spring Boot 自动配置、依赖注入、Properties 外部化配置等开发者熟悉的模式。它更适合已经在使用 Spring 技术栈的团队。

LangChain4j 是 LangChain 的 Java 版本，框架无关，可以在任何 Java 项目中使用。它的 API 设计更接近 Python 版 LangChain 的风格。

在模型支持方面，两个框架都覆盖了主流的 LLM 提供商。Spring AI 通过 Spring Boot Starter 提供自动配置，LangChain4j 则通过独立的模块来支持不同的提供商。

选择建议：如果你的项目已经是 Spring Boot，优先考虑 Spring AI，学习曲线更平滑。如果你需要框架无关的方案，或者已经在用 Quarkus/Micronaut，LangChain4j 是不错的选择。
