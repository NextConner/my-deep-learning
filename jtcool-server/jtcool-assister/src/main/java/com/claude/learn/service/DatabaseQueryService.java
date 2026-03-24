package com.claude.learn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DatabaseQueryService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseQueryService.class);

    private final JdbcTemplate jdbcTemplate;
    private final SecurityAuditService auditService;

    @Value("${agent.database.max-rows:100}")
    private int maxRows;

    @Value("${agent.database.query-timeout:5000}")
    private int queryTimeout;

    public DatabaseQueryService(JdbcTemplate jdbcTemplate, SecurityAuditService auditService) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditService = auditService;
    }

    public String executeQuery(String sql, String username) {
        validateSql(sql);
        String limitedSql = applySqlLimit(sql, maxRows);

        jdbcTemplate.setQueryTimeout(queryTimeout / 1000);

        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(limitedSql);
            auditService.logDatabaseQuery(username, sql, results.size());
            return formatResults(results);
        } catch (Exception e) {
            log.error("Database query failed", e);
            throw new RuntimeException("查询执行失败: " + e.getMessage());
        }
    }

    private void validateSql(String sql) {
        String upperSql = sql.toUpperCase().trim();
        if (!upperSql.startsWith("SELECT")) {
            throw new IllegalArgumentException("仅支持SELECT查询");
        }
        String[] blacklist = {"DROP", "DELETE", "UPDATE", "INSERT", "ALTER", "CREATE", "TRUNCATE", "EXEC", "EXECUTE"};
        for (String keyword : blacklist) {
            if (upperSql.contains(keyword)) {
                throw new IllegalArgumentException("SQL包含禁止的关键字: " + keyword);
            }
        }
    }

    private String applySqlLimit(String sql, int limit) {
        if (sql.toUpperCase().contains("LIMIT")) {
            return sql;
        }
        return sql + " LIMIT " + limit;
    }

    private String formatResults(List<Map<String, Object>> results) {
        if (results.isEmpty()) {
            return "查询结果为空";
        }
        StringBuilder sb = new StringBuilder();
        Set<String> columns = results.get(0).keySet();
        sb.append("| ").append(String.join(" | ", columns)).append(" |\n");
        sb.append("|").append(" --- |".repeat(columns.size())).append("\n");
        int displayRows = Math.min(results.size(), 20);
        for (int i = 0; i < displayRows; i++) {
            Map<String, Object> row = results.get(i);
            sb.append("| ");
            for (String col : columns) {
                sb.append(row.get(col)).append(" | ");
            }
            sb.append("\n");
        }
        if (results.size() > 20) {
            sb.append("\n... 共 ").append(results.size()).append(" 条记录");
        }
        return sb.toString();
    }
}