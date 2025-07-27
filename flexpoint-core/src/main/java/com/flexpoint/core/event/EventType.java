package com.flexpoint.core.event;

/**
 * 扩展点事件类型枚举
 * 用于标识不同的扩展点事件场景
 *
 * @author xiangganluo
 */
public enum EventType {
    // ==================== 扩展点生命周期事件 ====================
    /** 扩展点注册 */
    EXT_REGISTERED,
    /** 扩展点注销 */
    EXT_UNREGISTERED,
    /** 扩展点查找 */
    EXT_FOUND,
    /** 扩展点未找到 */
    EXT_NOT_FOUND,
    /** 扩展点选择 */
    EXT_SELECTED,
    /** 扩展点选择失败 */
    EXT_SELECTION_FAILED,
    
    // ==================== 扩展点调用事件 ====================
    /** 调用前 */
    INVOKE_BEFORE,
    /** 调用成功 */
    INVOKE_SUCCESS,
    /** 调用失败 */
    INVOKE_FAIL,
    /** 调用异常 */
    INVOKE_EXCEPTION,
    
    // ==================== 选择器事件 ====================
    /** 选择器注册 */
    SELECTOR_REGISTERED,
    /** 选择器注销 */
    SELECTOR_UNREGISTERED,
    /** 选择器查找 */
    SELECTOR_FOUND,
    /** 选择器未找到 */
    SELECTOR_NOT_FOUND
} 