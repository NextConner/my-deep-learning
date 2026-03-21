package com.claude.learn.service;


import com.claude.learn.domain.TokenUsage;
import com.claude.learn.repository.TokenUsageRepository;
import com.claude.learn.service.cache.TokenCacheStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenMonitorService {

    private static final Logger log = LoggerFactory.getLogger(TokenMonitorService.class);

    private final TokenUsageRepository tokenUsageRepository;
    private final TokenCacheStrategy cacheStrategy;

    public TokenMonitorService(
            TokenUsageRepository tokenUsageRepository,
            TokenCacheStrategy cacheStrategy) {
        this.tokenUsageRepository = tokenUsageRepository;
        this.cacheStrategy = cacheStrategy;
    }

    /**
     * 检查用户是否超额
     */
    public boolean isExceeded(String username) {
        int used = cacheStrategy.getUsed(username);
        int limit = cacheStrategy.getLimit(username);
        log.info("👤 用户 {} 今日已用 {}/{} tokens", username, used, limit);
        return used >= limit;
    }

    /**
     * 检查超额
     */
    @Transactional
    public void record(String username , int inputTokens , int outputTokens){

        int total = inputTokens + outputTokens;

        //持久化
        tokenUsageRepository.save(new TokenUsage(username,inputTokens,outputTokens));
        //更新缓存
        cacheStrategy.increment(username,total);
    }

    /**
     *  设置配额
     */
    public void setLimit(String username , int limit){
        cacheStrategy.setLimit(username,limit);
    }

     /**
     * 获取当前使用量
     */
     public String getUsageSummary(String username){
         int used = cacheStrategy.getUsed(username);
         int limit = cacheStrategy.getLimit(username);
         return String.format("今日已用 %d / %d tokens（%.1f%%）",
                 used, limit, (double) used / limit * 100);
     }

}
