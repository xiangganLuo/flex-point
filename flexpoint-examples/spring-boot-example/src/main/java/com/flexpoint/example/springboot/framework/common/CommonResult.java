package com.flexpoint.example.springboot.framework.common;

import lombok.Data;

/**
 * 通用结果
 * @author xiangganluo
 */
@Data
public class CommonResult<T> {
    
    /**
     * 错误码
     */
    private String code;
    
    /**
     * 错误信息
     */
    private String message;
    
    /**
     * 数据
     */
    private T data;
    
    /**
     * 成功结果
     */
    public static <T> CommonResult<T> success(T data) {
        CommonResult<T> result = new CommonResult<>();
        result.setCode("0");
        result.setMessage("success");
        result.setData(data);
        return result;
    }
    
    /**
     * 错误结果
     */
    public static <T> CommonResult<T> error(String code) {
        CommonResult<T> result = new CommonResult<>();
        result.setCode(code);
        result.setMessage("error");
        return result;
    }
    
    /**
     * 错误结果
     */
    public static <T> CommonResult<T> error(String code, String message) {
        CommonResult<T> result = new CommonResult<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
} 