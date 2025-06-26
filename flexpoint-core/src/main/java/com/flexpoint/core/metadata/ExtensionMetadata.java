package com.flexpoint.core.metadata;

/**
 * 扩展点元数据接口
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public interface ExtensionMetadata {
    
    /**
     * 获取扩展点ID
     */
    String getExtensionId();
    
    /**
     * 获取版本号
     */
    String getVersion();
    
    /**
     * 获取优先级
     */
    int getPriority();
    
    /**
     * 获取描述信息
     */
    String getDescription();
    
    /**
     * 是否启用
     */
    boolean isEnabled();
    
    /**
     * 获取扩展点类型
     */
    String getExtensionType();
    
    /**
     * 获取创建时间
     */
    long getCreateTime();
    
    /**
     * 获取更新时间
     */
    long getUpdateTime();
} 