package com.example.book.cache;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 演示素材：Redis 缓存集成 — 图书热点数据缓存
 */
@Service
public class RedisCacheService {

    private static final String BOOK_KEY_PREFIX = "book:detail:";
    private static final long TTL_MINUTES = 30;

    private final StringRedisTemplate redisTemplate;

    public RedisCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getBookJson(Long bookId) {
        String key = BOOK_KEY_PREFIX + bookId;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }
        String loaded = loadFromDb(bookId);
        if (loaded != null) {
            redisTemplate.opsForValue().set(key, loaded, TTL_MINUTES, TimeUnit.MINUTES);
        }
        return loaded;
    }

    public void evictBook(Long bookId) {
        redisTemplate.delete(BOOK_KEY_PREFIX + bookId);
    }

    private String loadFromDb(Long bookId) {
        // 演示：实际项目应调用 BookMapper
        return "{\"id\":" + bookId + ",\"title\":\"Java 编程思想\"}";
    }
}
