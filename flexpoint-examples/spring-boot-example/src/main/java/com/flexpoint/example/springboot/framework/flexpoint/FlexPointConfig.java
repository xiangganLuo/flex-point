package com.flexpoint.example.springboot.framework.flexpoint;

import com.flexpoint.core.selector.resolves.CodeVersionSelector;
import com.flexpoint.example.springboot.framework.flexpoint.context.SysAppContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 选择器链注册配置
 * @author luoxianggan
 */
@Component
@RequiredArgsConstructor
public class FlexPointConfig {

    /**
     * 注册默认的选择器链
     */
    @Bean
    public CodeVersionSelector codeVersionSelector() {
        return new CodeVersionSelector(context -> SysAppContext.getAppCode());
    }

}