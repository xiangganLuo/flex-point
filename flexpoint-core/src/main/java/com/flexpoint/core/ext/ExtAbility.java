package com.flexpoint.core.ext;

/**
 * 扩展点能力接口
 * 所有扩展点实现类都应该实现此接口
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public interface ExtAbility {

    /**
     * 获取业务标识
     * 用于区分不同业务场景的扩展点实现
     *
     * @return 业务标识
     */
    String getCode();

    /**
     * 获取扩展点标签
     * 所有扩展点属性都通过标签表达，包括版本号
     * 
     * @return 扩展点标签，默认为空标签
     */
    default ExtTags getTags() {
        return ExtTags.empty();
    }

}