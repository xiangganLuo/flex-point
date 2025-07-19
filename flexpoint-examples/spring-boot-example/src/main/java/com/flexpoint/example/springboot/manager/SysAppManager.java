package com.flexpoint.example.springboot.manager;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统应用管理器
 * @author xiangganluo
 */
@Service
public class SysAppManager {
    
    // 模拟数据库数据
    private static final Map<String, SysAppDO> APP_MAP = new HashMap<>();
    
    {
        // 初始化测试数据
        SysAppDO app1 = new SysAppDO();
        app1.setAppCode("mall-app");
        app1.setAppName("商城应用");
        app1.setToken("mall-token-123");
        app1.setIsActive(true);
        app1.setAppType("mall");
        APP_MAP.put("mall-app", app1);
        
        SysAppDO app2 = new SysAppDO();
        app2.setAppCode("logistics-app");
        app2.setAppName("物流应用");
        app2.setToken("logistics-token-456");
        app2.setIsActive(true);
        app2.setAppType("logistics");
        APP_MAP.put("logistics-app", app2);
        
        SysAppDO app3 = new SysAppDO();
        app3.setAppCode("finance-app");
        app3.setAppName("金融应用");
        app3.setToken("finance-token-789");
        app3.setIsActive(false);
        app3.setAppType("finance");
        APP_MAP.put("finance-app", app3);
    }
    
    /**
     * 根据应用编码获取应用信息
     */
    public SysAppDO getByCode(String appCode) {
        return APP_MAP.get(appCode);
    }


    /**
     * 系统应用数据对象
     */
    @Data
    public static class SysAppDO {

        /**
         * 应用编码
         */
        private String appCode;

        /**
         * 应用名称
         */
        private String appName;

        /**
         * 认证令牌
         */
        private String token;

        /**
         * 是否启用
         */
        private Boolean isActive;

        /**
         * 应用类型
         */
        private String appType;
    }
} 