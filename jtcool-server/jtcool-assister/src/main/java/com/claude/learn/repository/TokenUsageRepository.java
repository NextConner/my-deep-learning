package com.claude.learn.repository;

import com.claude.learn.domain.TokenUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenUsageRepository extends JpaRepository<TokenUsage,Long> {

    // 查询用户今日消耗总量
    @Query(value = """
            SELECT COALESCE(SUM(total_tokens), 0)
            FROM token_usage
            WHERE username = :username
            AND DATE(created_at) = CURRENT_DATE
            """, nativeQuery = true)
    int sumTodayUsage(@Param("username") String username);

}
