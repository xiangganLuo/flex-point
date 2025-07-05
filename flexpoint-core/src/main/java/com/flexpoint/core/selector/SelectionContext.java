package com.flexpoint.core.selector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 选择器上下文
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SelectionContext {

    /**
     * 标识
     */
    private String code;

    /**
     * 版本
     */
    private String version;

}
