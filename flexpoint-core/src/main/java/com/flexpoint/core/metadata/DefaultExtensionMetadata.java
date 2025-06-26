package com.flexpoint.core.metadata;

import lombok.Builder;
import lombok.Data;

/**
 * 默认扩展点元数据实现
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Data
@Builder
public class DefaultExtensionMetadata implements ExtensionMetadata {
    
    private String extensionId;
    private String version;
    private int priority;
    private String description;
    private boolean enabled;
    private String extensionType;
    private long createTime;
    private long updateTime;

}