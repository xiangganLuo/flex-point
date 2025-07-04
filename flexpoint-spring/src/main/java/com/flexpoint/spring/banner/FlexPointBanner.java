package com.flexpoint.spring.banner;

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
    
    public static final String BANNER =
            "  _   _   _   _   _   _   _   _   _ \n" +
            " / \\ / \\ / \\ / \\ / \\ / \\ / \\ / \\ / \\\n" +
            "( F | l | e | x | P | o | i | n | t )\n" +
            " \\_/ \\_/ \\_/ \\_/ \\_/ \\_/ \\_/ \\_/ \\\n";
    
    @Override
    public void afterPropertiesSet() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Flex Point:");
        sb.append("\n");
        sb.append(BANNER);
        System.out.println(sb);
    }

}