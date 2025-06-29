package com.flexpoint.core.resolution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>ResolutionContext</p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResolutionContext {

    /**
     * 标识
     */
    private String code;

    /**
     * 版本
     */
    private String version;

}
