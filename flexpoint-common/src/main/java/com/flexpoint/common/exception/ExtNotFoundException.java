package com.flexpoint.common.exception;

/**
 * 扩展点未找到异常
 * 当无法找到指定类型的扩展点实现时抛出此异常
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public class ExtNotFoundException extends RuntimeException {
    
    /**
     * 扩展点类型名称
     */
    private final String extType;
    
    /**
     * 选择器名称
     */
    private final String selectorName;
    
    /**
     * 扩展点代码
     */
    private final String extCode;
    
    private ExtNotFoundException(String message, String extType, String selectorName, String extCode) {
        super(message);
        this.extType = extType;
        this.selectorName = selectorName;
        this.extCode = extCode;
    }
    
    private ExtNotFoundException(String message, String extType, String selectorName, String extCode, Throwable cause) {
        super(message, cause);
        this.extType = extType;
        this.selectorName = selectorName;
        this.extCode = extCode;
    }
    
    /**
     * 创建基础的扩展点未找到异常
     */
    public static ExtNotFoundException forType(String extType) {
        return new ExtNotFoundException(
            String.format("未找到扩展点实现: type=%s", extType),
            extType, null, null);
    }
    
    /**
     * 创建选择器未找到扩展点的异常
     */
    public static ExtNotFoundException forSelector(String extType, String selectorName) {
        return new ExtNotFoundException(
            String.format("选择器[%s]未找到匹配的扩展点: type=%s", selectorName, extType),
            extType, selectorName, null);
    }
    
    /**
     * 创建包含扩展点代码的异常
     */
    public static ExtNotFoundException forCode(String extType, String selectorName, String extCode) {
        return new ExtNotFoundException(
            String.format("选择器[%s]未找到匹配的扩展点: type=%s, code=%s", selectorName, extType, extCode),
            extType, selectorName, extCode);
    }
    
    /**
     * 创建自定义消息的异常
     */
    public static ExtNotFoundException withMessage(String extType, String customMessage) {
        return new ExtNotFoundException(customMessage, extType, null, null);
    }
    
    /**
     * 创建带异常链的基础异常
     */
    public static ExtNotFoundException forType(String extType, Throwable cause) {
        return new ExtNotFoundException(
            String.format("未找到扩展点实现: type=%s", extType),
            extType, null, null, cause);
    }
    
    /**
     * 创建带异常链的选择器异常
     */
    public static ExtNotFoundException forSelector(String extType, String selectorName, Throwable cause) {
        return new ExtNotFoundException(
            String.format("选择器[%s]未找到匹配的扩展点: type=%s", selectorName, extType),
            extType, selectorName, null, cause);
    }
    
    /**
     * 获取扩展点类型名称
     * 
     * @return 扩展点类型名称
     */
    public String getExtType() {
        return extType;
    }
    
    /**
     * 获取选择器名称
     * 
     * @return 选择器名称，可能为null
     */
    public String getSelectorName() {
        return selectorName;
    }
    
    /**
     * 获取扩展点代码
     * 
     * @return 扩展点代码，可能为null
     */
    public String getExtCode() {
        return extCode;
    }
} 