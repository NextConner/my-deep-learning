package com.claude.learn.agent.runtime;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Agent运行记录 - 表示一次完整的Agent执行
 *
 * 核心职责：
 * 1. 记录执行的元数据（runId、用户、问题、时间）
 * 2. 管理执行步骤列表（AgentStep）
 * 3. 跟踪执行状态（RUNNING、SUCCESS、FAILED）
 * 4. 计算总执行时间
 *
 * 设计模式：
 * - 不可变对象（final字段）+ 状态转换方法
 * - 工厂方法模式（newStep创建步骤）
 * - 防御性拷贝（getSteps返回不可变列表）
 *
 * 面试要点：
 * - 为什么用UUID？分布式环境下保证唯一性，可用于分布式追踪
 * - 为什么返回unmodifiableList？防止外部修改内部状态，保证数据一致性
 * - 如何持久化？当前是内存对象，可扩展为JPA实体存储到数据库
 */
public class AgentRun {

    // 运行ID，使用UUID保证全局唯一性，可用作分布式追踪的traceId
    private final String runId;
    // 用户名，用于审计和配额控制
    private final String username;
    // 用户问题，原始输入
    private final String userQuestion;
    // 开始时间，用于计算总耗时
    private final LocalDateTime startedAt;
    // 执行步骤列表，记录完整的执行轨迹
    private final List<AgentStep> steps;
    // 执行状态，可变字段，通过markSuccess/markFailed修改
    private AgentRunStatus status;
    // 最终答案，成功时设置
    private String finalAnswer;
    // 结束时间，用于计算总耗时
    private LocalDateTime endedAt;

    /**
     * 构造函数：初始化运行记录
     * 自动生成runId和startedAt，初始状态为RUNNING
     */
    public AgentRun(String username, String userQuestion) {
        this.runId = UUID.randomUUID().toString();
        this.username = username;
        this.userQuestion = userQuestion;
        this.startedAt = LocalDateTime.now();
        this.steps = new ArrayList<>();
        this.status = AgentRunStatus.RUNNING;
    }

    /**
     * 工厂方法：创建新的执行步骤
     *
     * @param toolName 工具名称
     * @param inputSummary 输入摘要
     * @return 新创建的步骤，已添加到steps列表
     *
     * 面试要点：
     * - 为什么用工厂方法？封装步骤创建逻辑，自动管理sequence编号
     * - 为什么返回步骤对象？允许调用者继续操作步骤（start、markSuccess等）
     */
    public AgentStep newStep(String toolName, String inputSummary) {
        AgentStep step = new AgentStep(steps.size() + 1, toolName, inputSummary);
        this.steps.add(step);
        return step;
    }

    /**
     * 标记运行成功
     * 状态转换：RUNNING -> SUCCESS
     */
    public void markSuccess(String finalAnswer) {
        this.status = AgentRunStatus.SUCCESS;
        this.finalAnswer = finalAnswer;
        this.endedAt = LocalDateTime.now();
    }

    /**
     * 标记运行失败
     * 状态转换：RUNNING -> FAILED
     */
    public void markFailed() {
        this.status = AgentRunStatus.FAILED;
        this.endedAt = LocalDateTime.now();
    }

    /**
     * 计算总执行时间（毫秒）
     *
     * @return 总耗时，如果未结束返回0
     *
     * 面试要点：
     * - 为什么用Duration？Java 8时间API，类型安全，避免手动计算
     * - 如何优化？可以缓存计算结果，避免重复计算
     */
    public long totalLatencyMs() {
        if (endedAt == null) {
            return 0L;
        }
        return Duration.between(startedAt, endedAt).toMillis();
    }

    // Getters

    public String getRunId() {
        return runId;
    }

    public String getUsername() {
        return username;
    }

    public String getUserQuestion() {
        return userQuestion;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    /**
     * 获取步骤列表
     *
     * @return 不可变列表，防止外部修改
     *
     * 面试要点：
     * - 为什么返回unmodifiableList？防御性编程，保护内部状态
     * - 性能影响？Collections.unmodifiableList是O(1)操作，只是包装
     */
    public List<AgentStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public AgentRunStatus getStatus() {
        return status;
    }

    public String getFinalAnswer() {
        return finalAnswer;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }
}
