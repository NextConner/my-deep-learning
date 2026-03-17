# 第1周执行计划（Day1~Day7）

## 周目标
在当前项目中落地一个可观测、可控、可复盘的 Agent Loop，并具备最小可测能力。

## Day 1：定义运行模型与状态机
### 任务
1. 新增运行对象：`AgentRun`、`AgentStep`、`AgentStepStatus`。
2. 定义状态流转：`PLANNED -> RUNNING -> SUCCESS/FAILED/TIMEOUT`。
3. 约定统一错误码：`TOOL_TIMEOUT`、`TOOL_VALIDATION_FAIL`、`MAX_STEPS_EXCEEDED`。

### 建议落点
- `src/main/java/com/claude/learn/agent/runtime/AgentRun.java`
- `src/main/java/com/claude/learn/agent/runtime/AgentStep.java`
- `src/main/java/com/claude/learn/agent/runtime/AgentErrorCode.java`

## Day 2：实现 Agent Orchestrator（核心）
### 任务
1. 新增 `AgentOrchestratorService`，实现主循环：
   - 生成 plan
   - 挑选 tool
   - 执行 tool
   - 记录 observe
   - 反思并继续或收敛
2. 增加执行约束：`maxSteps`、`stepTimeoutMs`、`retryTimes`。

### 建议落点
- `src/main/java/com/claude/learn/service/AgentOrchestratorService.java`
- `src/main/java/com/claude/learn/config/AgentRuntimeProperties.java`

## Day 3：工具执行层标准化
### 任务
1. 抽象 `ToolExecutor` 接口（统一输入输出）。
2. 将现有工具调用包装为标准结果对象（含耗时、异常、重试次数）。
3. 对工具超时和异常进行归一化处理。

### 建议落点
- `src/main/java/com/claude/learn/agent/tool/ToolExecutor.java`
- `src/main/java/com/claude/learn/agent/tool/ToolExecutionResult.java`

## Day 4：可观测性与审计日志（最小版）
### 任务
1. 每轮输出 `traceId + runId + stepId`。
2. 记录每步：tool、输入摘要、输出摘要、耗时、token。
3. 将轨迹挂到 API 返回中（可配开关，仅开发环境显示）。

### 建议落点
- `src/main/java/com/claude/learn/controller/ChatController.java`
- `src/main/java/com/claude/learn/service/TokenMonitorService.java`

## Day 5：接入现有接口并保证兼容
### 任务
1. `/api/chat` 接入 orchestrator，保留原有返回结构兼容。
2. `/api/chat/stream` 支持按 step 推送事件（`plan`、`tool_call`、`tool_result`、`final`）。
3. 增加配置项 `agent.runtime.trace-enabled`。

### 建议落点
- `src/main/java/com/claude/learn/controller/ChatController.java`
- `src/main/resources/application.yml`

## Day 6：测试与回归
### 任务
1. 单元测试：
   - 正常多步
   - 超时
   - 重试后成功
   - 达到最大步数
2. 回归测试：原有聊天与上传能力不回退。

### 建议落点
- `src/test/java/com/claude/learn/AgentRuntimeTest.java`
- `src/test/java/com/claude/learn/ChatControllerRegressionTest.java`

## Day 7：文档与求职素材
### 任务
1. 输出时序图（Agent Loop）。
2. 更新 README 的“架构与执行流程”章节。
3. 写一页 STAR 案例：问题、约束、方案、结果。

### 建议落点
- `README.md`
- `docs/WEEK1_REVIEW.md`

## 本周验收标准
1. 可看到每次对话的 step 轨迹与耗时。
2. 工具超时和失败有明确错误码，不会无声失败。
3. 同一问题多次运行可复盘调用链。
4. 至少 4 个测试用例通过。

## 本周代码改造清单（最小实现）
1. 新增运行时模型包：`agent/runtime/*`。
2. 新增 orchestrator 服务并在 controller 中接入。
3. 工具执行封装为统一接口，不在 controller 直接散调。
4. 增加 runtime 配置项到 `application.yml`。
5. 增加测试类覆盖关键分支。
