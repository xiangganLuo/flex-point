package com.flexpoint.plugin.selector.tenant;

import com.flexpoint.core.context.FlexPointContext;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.ExtTags;
import com.flexpoint.core.selector.AbstractSelector;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 官方插件：租户选择器。
 *
 * <p>按 {@link FlexPointContext#getTenantId()} 过滤候选：命中条件为
 * {@code ext.getCode()} 等于该租户，或 tag {@code tenant} 等于该租户。</p>
 *
 * <p>构造参数 {@code fallback}：为 {@code true} 且当前租户无命中时，回退到「默认」候选——
 * tag {@code tenant} 缺省/空白，或 tag {@code tenant} 等于 {@value #DEFAULT_TENANT}，
 * 或 code 等于 {@value #DEFAULT_TENANT}。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public class TenantSelector extends AbstractSelector {

    /** 选择器名称。 */
    public static final String NAME = "tenantSelector";

    /** 租户标签键。 */
    public static final String TENANT_TAG = "tenant";

    /** 默认租户标识（用于 fallback）。 */
    public static final String DEFAULT_TENANT = "default";

    private final boolean fallback;

    /** 不启用回退。 */
    public TenantSelector() {
        this(false);
    }

    /**
     * @param fallback 租户无命中时是否回退到默认候选
     */
    public TenantSelector(boolean fallback) {
        this.fallback = fallback;
    }

    @Override
    protected <T extends ExtAbility> List<T> filter(List<T> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        String tenantId = FlexPointContext.current().getTenantId();
        if (tenantId != null && !tenantId.isEmpty()) {
            List<T> matched = candidates.stream()
                    .filter(ext -> matchesTenant(ext, tenantId))
                    .collect(Collectors.toList());
            log.debug("[{}] tenantId={}, 候选={}, 命中={}", NAME, tenantId, candidates.size(), matched.size());
            if (!matched.isEmpty() || !fallback) {
                return matched;
            }
            log.debug("[{}] 租户 {} 无命中，回退到默认候选", NAME, tenantId);
        } else {
            log.debug("[{}] 上下文无 tenantId", NAME);
            if (!fallback) {
                return Collections.emptyList();
            }
        }
        return candidates.stream().filter(this::isDefault).collect(Collectors.toList());
    }

    /** 候选是否属于指定租户。 */
    private boolean matchesTenant(ExtAbility ext, String tenantId) {
        if (tenantId.equals(ext.getCode())) {
            return true;
        }
        ExtTags tags = ext.getTags();
        return tags != null && tenantId.equals(tags.getString(TENANT_TAG));
    }

    /** 候选是否为默认（租户无关）候选。 */
    private boolean isDefault(ExtAbility ext) {
        if (DEFAULT_TENANT.equals(ext.getCode())) {
            return true;
        }
        ExtTags tags = ext.getTags();
        String tenant = tags == null ? null : tags.getString(TENANT_TAG);
        return tenant == null || tenant.isEmpty() || DEFAULT_TENANT.equals(tenant);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
