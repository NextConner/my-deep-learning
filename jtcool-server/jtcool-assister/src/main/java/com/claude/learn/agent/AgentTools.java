package com.claude.learn.agent;

import com.claude.learn.service.HybridSearchService;
import com.claude.learn.service.DatabaseQueryService;
import com.claude.learn.service.ToolPolicyGuardService;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AgentTools {


    private static final Logger log = LoggerFactory.getLogger(AgentTools.class);


    private final HybridSearchService hybridSearchService;
    private final ToolPolicyGuardService toolPolicyGuardService;
    private final DatabaseQueryService databaseQueryService;

    public AgentTools(HybridSearchService hybridSearchService,
                      ToolPolicyGuardService toolPolicyGuardService,
                      DatabaseQueryService databaseQueryService) {
        this.hybridSearchService = hybridSearchService;
        this.toolPolicyGuardService = toolPolicyGuardService;
        this.databaseQueryService = databaseQueryService;
    }

    /**
     * tools 定义,需要在LLM回答之前，传递过去
     * @param query
     * @return
     */

    @Tool("查询公司内部政策文档。当用户询问差旅相关费用标准、报销流程、审批规则时调用此工具，例如：机票报销、酒店住宿标准、打车费用上限、餐饮报销、商务舱审批等问题")
    public String searchPolicy(String query) {
        toolPolicyGuardService.checkToolAccess("searchPolicy");
        log.info("调用者：{}", Thread.currentThread().getStackTrace()[2].getClassName());
        var results = hybridSearchService.hybridSearch(query, 3);
        return String.join("\n", results);
    }

    @Tool("查询指定城市的实时天气")
    public String getWeather(String city){
        toolPolicyGuardService.checkToolAccess("getWeather");
        // 这里可以调用天气API获取实时天气信息
        // 由于这是一个示例，我们将返回一个固定的字符串
        log.info("getWeather:{}",city);
        return "当前" + city + "的天气是晴天，温度25摄氏度。";
    }

    @Tool("发送邮件给指定收件人")
    public String sendEmail(String recipient) {
        toolPolicyGuardService.checkToolAccess("sendEmail");
        // 模拟发送邮件
        return "邮件已发送给：" + recipient;
    }

    @Tool("查询业务数据库。支持查询订单、库存、商品等业务数据。参数是标准的SQL SELECT语句，例如：SELECT * FROM oms_order WHERE order_status='PENDING' LIMIT 10")
    public String queryDatabase(String sqlQuery) {
        toolPolicyGuardService.checkToolAccess("queryDatabase");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Database query by {}: {}", username, sqlQuery);
        return databaseQueryService.executeQuery(sqlQuery, username);
    }
}
