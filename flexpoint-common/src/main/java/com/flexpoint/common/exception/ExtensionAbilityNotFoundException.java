package com.flexpoint.common.exception;

/**
 * 扩展点未找到异常
 * 当无法找到指定类型的扩展点实现时抛出此异常
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public class ExtensionAbilityNotFoundException extends RuntimeException {
    
    /**
     * 扩展点类型名称
     */
    private final String extensionType;
    
    /**
     * 选择器名称
     */
    private final String selectorName;
    
    /**
     * 扩展点代码
     */
    private final String extensionCode;
    
    private ExtensionAbilityNotFoundException(String message, String extensionType, String selectorName, String extensionCode) {
        super(message);
        this.extensionType = extensionType;
        this.selectorName = selectorName;
        this.extensionCode = extensionCode;
    }
    
    private ExtensionAbilityNotFoundException(String message, String extensionType, String selectorName, String extensionCode, Throwable cause) {
        super(message, cause);
        this.extensionType = extensionType;
        this.selectorName = selectorName;
        this.extensionCode = extensionCode;
    }
    
    /**
     * 创建基础的扩展点未找到异常
     */
    public static ExtensionAbilityNotFoundException forType(String extensionType) {
        return new ExtensionAbilityNotFoundException(
            String.format("未找到扩展点实现: type=%s", extensionType),
            extensionType, null, null);
    }
    
    /**
     * 创建选择器未找到扩展点的异常
     */
    public static ExtensionAbilityNotFoundException forSelector(String extensionType, String selectorName) {
        return new ExtensionAbilityNotFoundException(
            String.format("选择器[%s]未找到匹配的扩展点: type=%s", selectorName, extensionType),
            extensionType, selectorName, null);
    }
    
    /**
     * 创建包含扩展点代码的异常
     */
    public static ExtensionAbilityNotFoundException forCode(String extensionType, String selectorName, String extensionCode) {
        return new ExtensionAbilityNotFoundException(
            String.format("选择器[%s]未找到匹配的扩展点: type=%s, code=%s", selectorName, extensionType, extensionCode),
            extensionType, selectorName, extensionCode);
    }
    
    /**
     * 创建自定义消息的异常
     */
    public static ExtensionAbilityNotFoundException withMessage(String extensionType, String customMessage) {
        return new ExtensionAbilityNotFoundException(customMessage, extensionType, null, null);
    }
    
    /**
     * 创建带异常链的基础异常
     */
    public static ExtensionAbilityNotFoundException forType(String extensionType, Throwable cause) {
        return new ExtensionAbilityNotFoundException(
            String.format("未找到扩展点实现: type=%s", extensionType),
            extensionType, null, null, cause);
    }
    
    /**
     * 创建带异常链的选择器异常
     */
    public static ExtensionAbilityNotFoundException forSelector(String extensionType, String selectorName, Throwable cause) {
        return new ExtensionAbilityNotFoundException(
            String.format("选择器[%s]未找到匹配的扩展点: type=%s", selectorName, extensionType),
            extensionType, selectorName, null, cause);
    }
    
    /**
     * 获取扩展点类型名称
     * 
     * @return 扩展点类型名称
     */
    public String getExtensionType() {
        return extensionType;
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
    public String getExtensionCode() {
        return extensionCode;
    }
} 