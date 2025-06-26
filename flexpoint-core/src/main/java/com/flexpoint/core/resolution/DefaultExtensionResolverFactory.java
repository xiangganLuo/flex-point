package com.flexpoint.core.resolution;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.flexpoint.core.resolution.DefaultExtensionResolutionStrategy.DEFAULT_RESOLVER_NAME;

/**
 * 默认扩展点解析器工厂实现
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class DefaultExtensionResolverFactory implements ExtensionResolverFactory {
    
    private final Map<String, ExtensionResolutionStrategy> resolvers = new ConcurrentHashMap<>();

    public DefaultExtensionResolverFactory() {
        // 注册默认解析器
        registerResolver(new DefaultExtensionResolutionStrategy());
    }
    
    @Override
    public ExtensionResolutionStrategy getResolver(String resolverName) {
        if (resolverName == null || resolverName.trim().isEmpty()) {
            // 返回默认解析器
            return resolvers.get(DEFAULT_RESOLVER_NAME);
        }
        
        ExtensionResolutionStrategy resolver = resolvers.get(resolverName);
        if (resolver == null) {
            log.warn("未找到解析器: {}", resolverName);
            return resolvers.get(DEFAULT_RESOLVER_NAME);
        }
        
        return resolver;
    }
    
    /**
     * 注册解析器
     *
     * @param name 解析器名称
     * @param resolver 解析器实例
     */
    @Override
    public void registerResolver(ExtensionResolutionStrategy resolver) {
        if (resolver == null || resolver.getStrategyName() == null) {
            log.warn("解析器注册失败: 解析器名称为空");
            return;
        }
        resolvers.put(resolver.getStrategyName(), resolver);
        log.info("注册解析器: {} -> {}", resolver.getStrategyName(), resolver.getClass().getSimpleName());
    }
} 