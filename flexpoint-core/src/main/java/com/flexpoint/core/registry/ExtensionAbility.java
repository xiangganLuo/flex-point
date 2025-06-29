package com.flexpoint.core.registry;

/**
 * 扩展点能力接口
 * 所有扩展点实现类都应该实现此接口
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public interface ExtensionAbility {

    /**
     * 获取业务标识
     * 用于区分不同业务场景的扩展点实现
     *
     * @return 业务标识
     */
    String getCode();

} 