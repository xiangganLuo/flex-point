package com.flexpoint.common.exception;

/**
 * 选择器未找到异常
 * 当无法找到指定名称的选择器时抛出此异常
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public class SelectorNotFoundException extends RuntimeException {
    
    /**
     * 选择器名称
     */
    private final String selectorName;
    
    /**
     * 扩展点类型名称
     */
    private final String extType;
    
    public SelectorNotFoundException(String selectorName) {
        super(String.format("未找到名称为[%s]的选择器，请检查选择器是否已注册", selectorName));
        this.selectorName = selectorName;
        this.extType = null;
    }
    
    public SelectorNotFoundException(String selectorName, String extType) {
        super(String.format("扩展点类型[%s]使用的选择器[%s]未找到，请检查选择器是否已注册", extType, selectorName));
        this.selectorName = selectorName;
        this.extType = extType;
    }
    
    public SelectorNotFoundException(String selectorName, String extType, String customMessage) {
        super(customMessage);
        this.selectorName = selectorName;
        this.extType = extType;
    }
    
    public SelectorNotFoundException(String selectorName, Throwable cause) {
        super(String.format("未找到名称为[%s]的选择器，请检查选择器是否已注册", selectorName), cause);
        this.selectorName = selectorName;
        this.extType = null;
    }
    
    public SelectorNotFoundException(String selectorName, String extType, Throwable cause) {
        super(String.format("扩展点类型[%s]使用的选择器[%s]未找到，请检查选择器是否已注册", extType, selectorName), cause);
        this.selectorName = selectorName;
        this.extType = extType;
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
     * 获取扩展点类型名称
     * 
     * @return 扩展点类型名称，可能为null
     */
    public String getExtType() {
        return extType;
    }
} 