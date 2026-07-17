package com.flexpoint.common.context;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 标准请求上下文（线程级）。
 *
 * <p>承载路由/治理常用的标准字段（tenantId/appCode/version/uid/labels）与任意扩展属性，
 * 通过 {@link ThreadLocal} 在一次请求内传递。路由类选择器直接读取本上下文即可完成路由，
 * 无需业务方编写 Resolver——配合接入层（如 Web Filter）在入口填充上下文，实现「配置即装配」。</p>
 *
 * <p>置于 {@code flexpoint-common}（跨层共享的基础模块）：接入层填充、选择器（插件层）读取，
 * 属于框架的横切基础设施，不隶属内核。</p>
 *
 * <p>使用：
 * <pre>
 *   // 入口填充
 *   FlexPointContext.current().setTenantId("t1").setAppCode("mall").label("region", "cn");
 *   // 选择器读取
 *   String code = FlexPointContext.current().getAppCode();
 *   // 请求结束清理
 *   FlexPointContext.clear();
 * </pre></p>
 *
 * @author xiangganluo
 */
public class FlexPointContext {

    private static final ThreadLocal<FlexPointContext> HOLDER = new ThreadLocal<>();

    @Getter @Setter private String tenantId;
    @Getter @Setter private String appCode;
    @Getter @Setter private String version;
    @Getter @Setter private String uid;

    private final Map<String, String> labels = new HashMap<>();
    private final Map<String, Object> attributes = new HashMap<>();

    // =============== 标准字段链式设置 ===============

    public FlexPointContext tenantId(String v) { this.tenantId = v; return this; }
    public FlexPointContext appCode(String v) { this.appCode = v; return this; }
    public FlexPointContext version(String v) { this.version = v; return this; }
    public FlexPointContext uid(String v) { this.uid = v; return this; }

    // =============== labels ===============

    /** 设置一个标签 */
    public FlexPointContext label(String key, String value) {
        if (key != null && value != null) {
            labels.put(key, value);
        }
        return this;
    }

    public String getLabel(String key) {
        return labels.get(key);
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    // =============== attributes（任意类型） ===============

    public FlexPointContext attr(String key, Object value) {
        if (key != null) {
            attributes.put(key, value);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttr(String key) {
        return (T) attributes.get(key);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    // =============== ThreadLocal 持有 ===============

    /** 获取当前线程上下文（不存在则创建并绑定）。 */
    public static FlexPointContext current() {
        FlexPointContext ctx = HOLDER.get();
        if (ctx == null) {
            ctx = new FlexPointContext();
            HOLDER.set(ctx);
        }
        return ctx;
    }

    /** 仅查看当前线程上下文，不创建。 */
    public static Optional<FlexPointContext> peek() {
        return Optional.ofNullable(HOLDER.get());
    }

    /** 绑定上下文到当前线程。 */
    public static void set(FlexPointContext ctx) {
        HOLDER.set(ctx);
    }

    /** 清理当前线程上下文（请求结束必须调用，避免线程池串扰）。 */
    public static void clear() {
        HOLDER.remove();
    }

    @Override
    public String toString() {
        return "FlexPointContext{tenantId=" + tenantId + ", appCode=" + appCode
                + ", version=" + version + ", uid=" + uid
                + ", labels=" + labels + ", attributes=" + attributes.keySet() + '}';
    }
}
