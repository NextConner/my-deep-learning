package com.claude.learn.agent;

import com.claude.learn.service.HybridSearchService;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AgentTools {


    private static final Logger log = LoggerFactory.getLogger(AgentTools.class);


    private final HybridSearchService hybridSearchService;

    public AgentTools(HybridSearchService hybridSearchService) {
        this.hybridSearchService = hybridSearchService;
    }

    /**
     * tools 定义,需要在LLM回答之前，传递过去
     * @param query
     * @return
     */

    @Tool("查询公司内部政策文档。当用户询问差旅相关费用标准、报销流程、审批规则时调用此工具，例如：机票报销、酒店住宿标准、打车费用上限、餐饮报销、商务舱审批等问题")
    public String searchPolicy(String query) {
        log.info("调用者：{}", Thread.currentThread().getStackTrace()[2].getClassName());
        var results = hybridSearchService.hybridSearch(query, 3);
        return String.join("\n", results);
    }

    @Tool("查询指定城市的实时天气")
    public String getWeather(String city){
        // 这里可以调用天气API获取实时天气信息
        // 由于这是一个示例，我们将返回一个固定的字符串
        log.info("getWeather:{}",city);
        return "当前" + city + "的天气是晴天，温度25摄氏度。";
    }

    @Tool("发送邮件给指定收件人")
    public String sendEmail(String recipient) {
        // 模拟发送邮件
        return "邮件已发送给：" + recipient;
    }
}
