package com.flexpoint.common.exception;

/**
 * 选择器匹配到多个扩展点实现异常
 * 当选择器期望唯一结果但匹配到多个实现时抛出此异常
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public class MultipleExtMatchedException extends RuntimeException {
    
    /**
     * 选择器名称
     */
    private final String selectorName;
    
    /**
     * 匹配到的扩展点数量
     */
    private final int matchedCount;
    
    public MultipleExtMatchedException(String selectorName, int matchedCount) {
        super(String.format("选择器[%s]匹配到多个扩展点实现，期望唯一结果。匹配数量: %d，请优化选择器逻辑确保结果唯一性。", 
            selectorName, matchedCount));
        this.selectorName = selectorName;
        this.matchedCount = matchedCount;
    }
    
    public MultipleExtMatchedException(String selectorName, int matchedCount, String customMessage) {
        super(customMessage);
        this.selectorName = selectorName;
        this.matchedCount = matchedCount;
    }
    
    public MultipleExtMatchedException(String selectorName, int matchedCount, Throwable cause) {
        super(String.format("选择器[%s]匹配到多个扩展点实现，期望唯一结果。匹配数量: %d，请优化选择器逻辑确保结果唯一性。", 
            selectorName, matchedCount), cause);
        this.selectorName = selectorName;
        this.matchedCount = matchedCount;
    }
    
    /**
     * 获取选择器名称
     * 
     * @return 选择器名称
     */
    public String getSelectorName() {
        return selectorName;
    }
    
    /**
     * 获取匹配到的扩展点数量
     * 
     * @return 匹配数量
     */
    public int getMatchedCount() {
        return matchedCount;
    }
} 