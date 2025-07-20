package com.flexpoint.spring.banner;

import com.flexpoint.common.constants.FlexPointConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

/**
 * Flex Point Banner打印器
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class FlexPointBanner implements InitializingBean {

    @Override
    public void afterPropertiesSet() {
        StringBuilder sb = new StringBuilder();
        sb.append("Flex Point:");
        sb.append("\n");
        sb.append(FlexPointConstants.BANNER);
        System.out.println(sb);
    }

}