package com.flexpoint.core.resolution;

/**
 * 扩展点解析器工厂接口
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public interface ExtensionResolverFactory {
    
    /**
     * 获取解析器
     *
     * @param resolverName 解析器名称
     * @return 解析器实例
     */
    ExtensionResolutionStrategy getResolver(String resolverName);
} 