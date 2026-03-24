package com.claude.learn.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class QuestionSuggestionService {

    private static final Map<String, List<String>> MODULE_QUESTIONS = Map.of(
        "OMS", List.of(
            "查询今日订单统计",
            "显示待发货订单列表",
            "分析本月销售趋势",
            "查看退款订单"
        ),
        "WMS", List.of(
            "查看库存预警商品",
            "统计今日入库出库记录",
            "显示仓库容量使用率",
            "查询指定商品库存"
        ),
        "PRODUCT", List.of(
            "查询热销商品TOP10",
            "显示品牌分布统计",
            "分析商品类目结构",
            "查看新增商品"
        ),
        "GENERAL", List.of(
            "系统使用帮助",
            "查看我的权限",
            "最近操作记录"
        )
    );

    public List<String> getSuggestions(String module) {
        return MODULE_QUESTIONS.getOrDefault(module, MODULE_QUESTIONS.get("GENERAL"));
    }
}
