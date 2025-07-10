package com.flexpoint.example.java.context;

/**
 * Java原生环境下的应用上下文（仿照Spring Boot SysAppContext）
 */
public class AppContext {
    private static final ThreadLocal<String> APP_CODE_HOLDER = new ThreadLocal<>();

    public static void setAppCode(String appCode) {
        APP_CODE_HOLDER.set(appCode);
    }

    public static String getAppCode() {
        return APP_CODE_HOLDER.get();
    }

    public static void clear() {
        APP_CODE_HOLDER.remove();
    }
} 