package com.flexpoint.core.context;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 选择器上下文，支持多维扩展。
 * @author xiangganluo
 */
@Getter
public class Context {
    private final Map<String, Object> attributes = new HashMap<>();

    public Context() {}
    public Context(Map<String, Object> attributes) {
        if (attributes != null) {
            this.attributes.putAll(attributes);
        }
    }

    public void set(String key, Object value) {
        attributes.put(key, value);
    }
    public Object get(String key) {
        return attributes.get(key);
    }
    public String getString(String key) {
        Object v = attributes.get(key);
        return v != null ? v.toString() : null;
    }
    public Integer getInt(String key) {
        Object v = attributes.get(key);
        return v instanceof Integer ? (Integer) v : null;
    }
}
