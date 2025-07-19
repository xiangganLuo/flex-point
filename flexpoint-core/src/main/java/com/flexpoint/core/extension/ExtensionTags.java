package com.flexpoint.core.extension;

import java.util.*;

/**
 * 扩展点标签系统
 * 完全抽象化的键值对存储，类似HTTP请求头和RPC元数据
 * 不包含任何业务概念，支持任意场景的标签存储
 */
public class ExtensionTags {
    
    private final Map<String, Object> tags;
    
    private ExtensionTags(Map<String, Object> tags) {
        this.tags = new HashMap<>(tags);
    }
    
    public static ExtensionTags empty() {
        return new ExtensionTags(Collections.emptyMap());
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // 基础标签操作方法
    public Object get(String key) {
        return tags.get(key);
    }
    
    public String getString(String key) {
        Object value = tags.get(key);
        return value != null ? value.toString() : null;
    }
    
    public String getString(String key, String defaultValue) {
        String value = getString(key);
        return value != null ? value : defaultValue;
    }
    
    public Integer getInt(String key) {
        Object value = tags.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    public Boolean getBoolean(String key) {
        Object value = tags.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public Set<String> getSet(String key) {
        Object value = tags.get(key);
        if (value instanceof Set) {
            return (Set<String>) value;
        }
        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty()) {
                return Collections.emptySet();
            }
            return new HashSet<>(Arrays.asList(str.split("[,;\\s]+")));
        }
        return Collections.emptySet();
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getList(String key) {
        Object value = tags.get(key);
        if (value instanceof List) {
            return (List<String>) value;
        }
        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty()) {
                return Collections.emptyList();
            }
            return Arrays.asList(str.split("[,;\\s]+"));
        }
        return Collections.emptyList();
    }
    
    public boolean has(String key) {
        return tags.containsKey(key);
    }
    
    public boolean has(String key, Object value) {
        return Objects.equals(tags.get(key), value);
    }
    
    public Map<String, Object> getAll() {
        return Collections.unmodifiableMap(tags);
    }
    
    public int size() {
        return tags.size();
    }
    
    public boolean isEmpty() {
        return tags.isEmpty();
    }
    
    public Set<String> keySet() {
        return Collections.unmodifiableSet(tags.keySet());
    }
    
    // Builder模式
    public static class Builder {
        private final Map<String, Object> tags = new HashMap<>();
        
        public Builder set(String key, Object value) {
            if (key != null && value != null) {
                tags.put(key, value);
            }
            return this;
        }
        
        public Builder setAll(Map<String, Object> tags) {
            if (tags != null) {
                this.tags.putAll(tags);
            }
            return this;
        }
        
        public ExtensionTags build() {
            return new ExtensionTags(tags);
        }
    }
} 