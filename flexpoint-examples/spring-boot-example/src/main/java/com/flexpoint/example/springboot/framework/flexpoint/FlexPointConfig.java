package com.flexpoint.example.springboot.framework.flexpoint;

import com.flexpoint.core.selector.resolves.CodeVersionSelector;
import com.flexpoint.example.springboot.framework.flexpoint.context.SysAppContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FlexPoint 配置
 * @author xiangganluo
 */
@Configuration
public class FlexPointConfig {

    /**
     * 注册默认的选择器
     */
    @Bean
    public CodeVersionSelector codeVersionSelector() {
        // 创建CodeVersionResolver实现
        CodeVersionSelector.CodeVersionResolver resolver = new CodeVersionSelector.CodeVersionResolver() {
            @Override
            public String resolveCode() {
                return SysAppContext.getAppCode();
            }
            
            @Override
            public String resolveVersion() {
                // 默认返回1.0.0，可以根据实际需要从其他地方获取版本信息
                return "1.0.0";
            }
        };
        
        return new CodeVersionSelector(resolver);
    }

}