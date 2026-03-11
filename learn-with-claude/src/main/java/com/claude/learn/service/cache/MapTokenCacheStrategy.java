package com.claude.learn.service.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MapTokenCacheStrategy implements TokenCacheStrategy {


    private static final Logger log = LoggerFactory.getLogger(MapTokenCacheStrategy.class);
    private static final int DEFAULT_DAILY_LIMIT = 10000000;
    private static final int MAX_CACHE_SIZE = 1000; // 最多缓存 1000 个用户

    // LRU Map：超过容量自动淘汰最久未使用的 key
    private final Map<String, AtomicInteger> usageMap = Collections.synchronizedMap(
            new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, AtomicInteger> eldest) {
                    boolean shouldRemove = size() > MAX_CACHE_SIZE;
                    if (shouldRemove) {
                        log.info("♻️ LRU 淘汰 key：{}", eldest.getKey());
                    }
                    return shouldRemove;
                }
            }
    );

    private final Map<String, Integer> limitMap = Collections.synchronizedMap(
            new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            }
    );

    private String usageKey(String username) {
        return username + ":" + LocalDate.now();
    }

    @Override
    public int getUsed(String username) {
        AtomicInteger used = usageMap.get(usageKey(username));
        return used != null ? used.get() : 0;
    }

    @Override
    public void increment(String username, int tokens) {
        usageMap.computeIfAbsent(usageKey(username), k -> new AtomicInteger(0))
                .addAndGet(tokens);
        log.info("📊 Token 记录：用户 {}，本次 +{}，今日累计 {}",
                username, tokens, getUsed(username));
    }

    @Override
    public int getLimit(String username) {
        return limitMap.getOrDefault(username, DEFAULT_DAILY_LIMIT);
    }

    @Override
    public void setLimit(String username, int limit) {
        limitMap.put(username, limit);
        log.info("⚙️ 用户 {} 配额设置为 {}", username, limit);
    }

    @Override
    public void reset(String username) {
        usageMap.remove(usageKey(username));
        log.info("🔄 用户 {} 今日 Token 已重置", username);
    }
}