package com.flexpoint.example.springboot.framework.flexpoint.context;

/**
 * 系统应用上下文
 */
public class SysAppContext {
    
    private static final ThreadLocal<String> APP_CODE_HOLDER = new ThreadLocal<>();
    
    /**
     * 设置应用编码
     */
    public static void setAppCode(String appCode) {
        APP_CODE_HOLDER.set(appCode);
    }
    
    /**
     * 获取应用编码
     */
    public static String getAppCode() {
        return APP_CODE_HOLDER.get();
    }
    
    /**
     * 清除上下文
     */
    public static void clear() {
        APP_CODE_HOLDER.remove();
    }
} 