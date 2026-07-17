package com.flexpoint.plugin.selector.tenant;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.ExtTags;
import com.flexpoint.core.selector.SelectionResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TenantSelector} 单元测试。
 *
 * @author xiangganluo
 */
class TenantSelectorTest {

    static class TenantExt implements ExtAbility {
        private final String extId;
        private final String code;
        private final ExtTags tags;

        TenantExt(String extId, String code, ExtTags tags) {
            this.extId = extId;
            this.code = code;
            this.tags = tags;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public ExtTags getTags() {
            return tags;
        }

        @Override
        public String getExtId() {
            return extId;
        }
    }

    @Test
    void hitByTenantTag() {
        TenantExt t1 = new TenantExt("t1", "impl", ExtTags.builder().set("tenant", "t1").build());
        TenantExt t2 = new TenantExt("t2", "impl", ExtTags.builder().set("tenant", "t2").build());
        List<ExtAbility> candidates = Arrays.asList(t1, t2);

        SelectionResult<ExtAbility> result = new TenantSelector(() -> "t1").select(candidates);

        assertTrue(result.isHit());
        assertEquals("t1", result.getSelected().getExtId());
    }

    @Test
    void hitByCode() {
        TenantExt t1 = new TenantExt("byCode", "t1", ExtTags.empty());
        TenantExt other = new TenantExt("other", "t2", ExtTags.empty());
        List<ExtAbility> candidates = Arrays.asList(t1, other);

        SelectionResult<ExtAbility> result = new TenantSelector(() -> "t1").select(candidates);

        assertTrue(result.isHit());
        assertEquals("byCode", result.getSelected().getExtId());
    }

    @Test
    void missWhenNoTenantMatchAndNoFallback() {
        TenantExt t1 = new TenantExt("t1", "impl", ExtTags.builder().set("tenant", "t1").build());
        List<ExtAbility> candidates = Arrays.asList((ExtAbility) t1);

        SelectionResult<ExtAbility> result = new TenantSelector(false, () -> "unknown").select(candidates);

        assertTrue(result.isMiss());
    }

    @Test
    void fallbackToDefaultCandidateWhenTenantMissing() {
        TenantExt t1 = new TenantExt("t1", "impl", ExtTags.builder().set("tenant", "t1").build());
        TenantExt def = new TenantExt("def", "impl", ExtTags.builder().set("tenant", "default").build());
        List<ExtAbility> candidates = Arrays.asList(t1, def);

        SelectionResult<ExtAbility> result = new TenantSelector(true, () -> "unknown").select(candidates);

        assertTrue(result.isHit());
        assertEquals("def", result.getSelected().getExtId());
    }

    @Test
    void fallbackToTagLessCandidate() {
        TenantExt t1 = new TenantExt("t1", "impl", ExtTags.builder().set("tenant", "t1").build());
        TenantExt generic = new TenantExt("generic", "impl", ExtTags.empty());
        List<ExtAbility> candidates = Arrays.asList(t1, generic);

        SelectionResult<ExtAbility> result = new TenantSelector(true, () -> "unknown").select(candidates);

        assertTrue(result.isHit());
        assertEquals("generic", result.getSelected().getExtId());
    }

    @Test
    void missWhenNoTenantIdAndNoFallback() {
        TenantExt t1 = new TenantExt("t1", "impl", ExtTags.builder().set("tenant", "t1").build());
        List<ExtAbility> candidates = Arrays.asList((ExtAbility) t1);

        SelectionResult<ExtAbility> result = new TenantSelector(false, () -> null).select(candidates);

        assertTrue(result.isMiss());
    }
}
