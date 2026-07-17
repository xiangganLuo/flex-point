package com.flexpoint.plugin.selector.gray;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.ExtTags;
import com.flexpoint.core.selector.SelectionResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link GraySelector} 单元测试。
 *
 * @author xiangganluo
 */
class GraySelectorTest {

    static class GrayExt implements ExtAbility {
        private final String code;
        private final ExtTags tags;

        GrayExt(String code, ExtTags tags) {
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
            return code;
        }
    }

    private final GrayExt grayExt = new GrayExt("gray", ExtTags.builder().set("gray", "true").build());
    private final GrayExt groupGrayExt = new GrayExt("groupGray", ExtTags.builder().set("group", "gray").build());
    private final GrayExt stableExt = new GrayExt("stable", ExtTags.empty());

    @Test
    void percentage100ShouldHitGrayCandidate() {
        List<ExtAbility> candidates = Arrays.asList(grayExt, stableExt);

        SelectionResult<ExtAbility> result = new GraySelector(100, () -> "user-1").select(candidates);

        assertTrue(result.isHit());
        assertEquals("gray", result.getSelected().getCode());
    }

    @Test
    void percentage0ShouldHitStableCandidate() {
        List<ExtAbility> candidates = Arrays.asList(grayExt, stableExt);

        SelectionResult<ExtAbility> result = new GraySelector(0, () -> "user-1").select(candidates);

        assertTrue(result.isHit());
        assertEquals("stable", result.getSelected().getCode());
    }

    @Test
    void groupGrayTagShouldBeTreatedAsGray() {
        List<ExtAbility> candidates = Arrays.asList(groupGrayExt, stableExt);

        SelectionResult<ExtAbility> result = new GraySelector(100, () -> "user-1").select(candidates);

        assertTrue(result.isHit());
        assertEquals("groupGray", result.getSelected().getCode());
    }

    @Test
    void missingKeyShouldFallBackToNonGray() {
        List<ExtAbility> candidates = Arrays.asList(grayExt, stableExt);
        // key 为空 -> 非灰度
        SelectionResult<ExtAbility> result = new GraySelector(100, () -> null).select(candidates);

        assertTrue(result.isHit());
        assertEquals("stable", result.getSelected().getCode());
    }

    @Test
    void customKeyResolverShouldWork() {
        List<ExtAbility> candidates = Arrays.asList(grayExt, stableExt);

        SelectionResult<ExtAbility> result = new GraySelector(100, () -> "d-123").select(candidates);

        assertTrue(result.isHit());
        assertEquals("gray", result.getSelected().getCode());
    }

    @Test
    void sameKeyShouldBeDeterministic() {
        List<ExtAbility> candidates = Arrays.asList(grayExt, stableExt);
        GraySelector selector = new GraySelector(50, () -> "determinism-key");

        String first = selector.select(candidates).getExplanation().getOutcome().name()
                + ":" + safeCode(selector.select(candidates));
        String second = selector.select(candidates).getExplanation().getOutcome().name()
                + ":" + safeCode(selector.select(candidates));

        assertEquals(first, second);
    }

    private String safeCode(SelectionResult<ExtAbility> r) {
        return r.getSelected() == null ? "null" : r.getSelected().getCode();
    }
}
