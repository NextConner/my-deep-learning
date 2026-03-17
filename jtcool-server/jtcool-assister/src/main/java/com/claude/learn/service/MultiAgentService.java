package com.claude.learn.service;

import com.claude.learn.agent.OrchestratorAgent;
import com.claude.learn.agent.PolicyAgent;
import com.claude.learn.agent.SummaryAgent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.annotations.Since;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MultiAgentService {

    private static final Logger log = LoggerFactory.getLogger(MultiAgentService.class);

    private final OrchestratorAgent orchestratorAgent;
    private final PolicyAgent policyAgent;
    private final HybridSearchService hybridSearchService;
    private final SummaryAgent summaryAgent;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 使用虚拟线程池，Java 21 特性，非常适合 I/O 密集型的 Agent 任务
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public MultiAgentService(
            OrchestratorAgent orchestratorAgent,
            PolicyAgent policyAgent,
            HybridSearchService hybridSearchService,
            SummaryAgent summaryAgent) {
        this.orchestratorAgent = orchestratorAgent;
        this.policyAgent = policyAgent;
        this.hybridSearchService = hybridSearchService;
        this.summaryAgent = summaryAgent;
    }


    /**
     * 模拟多agent 流程
     */
    public String run(String userQuestion)throws  Exception{

        log.info("用户问题：{}",userQuestion);

        //1. OrchestratorAgent 解析用户问题，决定需要哪些信息
        String plan = orchestratorAgent.plan(userQuestion);
        log.info("任务拆解结果：{}",plan);
        String cleanPlan = plan.replaceAll("```json|```", "").trim();
        //2.解析任务
        JsonNode tasks = objectMapper.readTree(cleanPlan).get("tasks");

        //3.并行执行任务
        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (JsonNode task : tasks) {
            String type = task.get("type").asText();
            String query = task.get("query").asText();

            CompletableFuture subTask = CompletableFuture.supplyAsync(()->{
               log.info("执行子任务：{}，查询：{}",type,query);
                return switch (type){
                    case "weather" -> {
                        //直接查询天气
                        yield switch (query.contains("北京") ? "北京" :
                                query.contains("上海") ? "上海" :
                                        query.contains("广州") ? "广州" : "其他") {
                            case "北京" -> "北京今日天气：小雨，温度12°C，建议带伞";
                            case "上海" -> "上海今日天气：多云，温度18°C，无需带伞";
                            case "广州" -> "广州今日天气：晴，温度26°C，注意防晒";
                            default -> "今日天气：晴，温度20°C";
                        };
                    }
                    case "policy" -> {
                        //RAG检索
                        var results = hybridSearchService.hybridSearch(query,3);
                        yield  String.join("\n",results);
                    }
                    default -> "未知任务类型：" + type;
                };
            },executor);
            futures.add(subTask);
        }

        //等待所有任务完成
       CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        //汇总结果
        StringBuilder allResults = new StringBuilder();
        allResults.append("用户问题：").append(userQuestion).append("\n\n");
        for (CompletableFuture<String> future : futures) {
            allResults.append(future.get()).append("\n\n");
        }
        log.info("所有子任务已完成.");

        //agent 汇总
        return  summaryAgent.summarize(allResults.toString());

    }


}
