package com.flexpoint.core.cache;

import com.flexpoint.common.ExtensionAbility;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 默认扩展点缓存管理器实现
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class DefaultExtensionCacheManager implements ExtensionCacheManager {
    
    // 缓存存储: Class -> Map<key, CacheEntry>
    private final ConcurrentHashMap<Class<? extends ExtensionAbility>, ConcurrentHashMap<String, CacheEntry>> cache =
            new ConcurrentHashMap<>();
    
    // 统计信息
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    
    @Override
    public <T extends ExtensionAbility> T getCachedExtension(Class<T> extensionType, String key) {
        ConcurrentHashMap<String, CacheEntry> typeCache = cache.get(extensionType);
        if (typeCache == null) {
            missCount.incrementAndGet();
            return null;
        }
        
        CacheEntry entry = typeCache.get(key);
        if (entry == null || entry.isExpired()) {
            missCount.incrementAndGet();
            if (entry != null && entry.isExpired()) {
                typeCache.remove(key);
            }
            return null;
        }
        
        hitCount.incrementAndGet();
        log.debug("缓存命中: type={}, key={}", extensionType.getSimpleName(), key);
        return (T) entry.getExtension();
    }
    
    @Override
    public <T extends ExtensionAbility> void cacheExtension(Class<T> extensionType, String key, T extension) {
        // 默认30分钟过期
        cacheExtension(extensionType, key, extension, 30, TimeUnit.MINUTES);
    }
    
    @Override
    public <T extends ExtensionAbility> void cacheExtension(Class<T> extensionType, String key, T extension,
                                                            long expireTime, TimeUnit timeUnit) {
        ConcurrentHashMap<String, CacheEntry> typeCache = cache.computeIfAbsent(extensionType, k -> new ConcurrentHashMap<>());
        CacheEntry entry = new CacheEntry(extension, System.currentTimeMillis() + timeUnit.toMillis(expireTime));
        typeCache.put(key, entry);
        
        log.debug("缓存扩展点: type={}, key={}, expireTime={}ms", 
                extensionType.getSimpleName(), key, timeUnit.toMillis(expireTime));
    }
    
    @Override
    public void invalidateCache(Class<? extends ExtensionAbility> extensionType) {
        cache.remove(extensionType);
        log.info("缓存已失效: type={}", extensionType.getSimpleName());
    }
    
    @Override
    public void invalidateCache(Class<? extends ExtensionAbility> extensionType, String key) {
        ConcurrentHashMap<String, CacheEntry> typeCache = cache.get(extensionType);
        if (typeCache != null) {
            typeCache.remove(key);
            log.debug("缓存键已失效: type={}, key={}", extensionType.getSimpleName(), key);
        }
    }
    
    @Override
    public void clearAllCache() {
        cache.clear();
        hitCount.set(0);
        missCount.set(0);
        log.info("所有缓存已清空");
    }
    
    @Override
    public CacheStatistics getCacheStatistics() {
        long hit = hitCount.get();
        long miss = missCount.get();
        long total = hit + miss;
        double hitRate = total > 0 ? (double) hit / total : 0.0;
        
        long cacheSize = cache.values().stream()
                .mapToLong(ConcurrentHashMap::size)
                .sum();
        
        return new CacheStatistics() {
            @Override
            public long getHitCount() {
                return hit;
            }
            
            @Override
            public long getMissCount() {
                return miss;
            }
            
            @Override
            public double getHitRate() {
                return hitRate;
            }
            
            @Override
            public long getCacheSize() {
                return cacheSize;
            }
        };
    }
    
    /**
     * 缓存条目
     */
    private static class CacheEntry {
        private final ExtensionAbility extension;
        private final long expireTime;
        
        public CacheEntry(ExtensionAbility extension, long expireTime) {
            this.extension = extension;
            this.expireTime = expireTime;
        }
        
        public ExtensionAbility getExtension() {
            return extension;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
} 