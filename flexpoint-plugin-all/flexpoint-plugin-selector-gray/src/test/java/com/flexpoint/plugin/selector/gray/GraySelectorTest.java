package com.flexpoint.plugin.selector.gray;

import com.flexpoint.core.context.FlexPointContext;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.ExtTags;
import com.flexpoint.core.selector.SelectionResult;
import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
    void tearDown() {
        FlexPointContext.clear();
    }

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
        FlexPointContext.current().uid("user-1");

        SelectionResult<ExtAbility> result = new GraySelector(100).select(candidates);

        assertTrue(result.isHit());
        assertEquals("gray", result.getSelected().getCode());
    }

    @Test
    void percentage0ShouldHitStableCandidate() {
        List<ExtAbility> candidates = Arrays.asList(grayExt, stableExt);
        FlexPointContext.current().uid("user-1");

        SelectionResult<ExtAbility> result = new GraySelector(0).select(candidates);

        assertTrue(result.isHit());
        assertEquals("stable", result.getSelected().getCode());
    }

    @Test
    void groupGrayTagShouldBeTreatedAsGray() {
        List<ExtAbility> candidates = Arrays.asList(groupGrayExt, stableExt);
        FlexPointContext.current().uid("user-1");

        SelectionResult<ExtAbility> result = new GraySelector(100).select(candidates);

        assertTrue(result.isHit());
        assertEquals("groupGray", result.getSelected().getCode());
    }

    @Test
    void missingKeyShouldFallBackToNonGray() {
        List<ExtAbility> candidates = Arrays.asList(grayExt, stableExt);
        // 未设置 uid，key 为空 -> 非灰度

        SelectionResult<ExtAbility> result = new GraySelector(100).select(candidates);

        assertTrue(result.isHit());
        assertEquals("stable", result.getSelected().getCode());
    }

    @Test
    void byLabelKeyProviderShouldWork() {
        List<ExtAbility> candidates = Arrays.asList(grayExt, stableExt);
        FlexPointContext.current().label("deviceId", "d-123");

        SelectionResult<ExtAbility> result =
                new GraySelector(100, GraySelector.byLabel("deviceId")).select(candidates);

        assertTrue(result.isHit());
        assertEquals("gray", result.getSelected().getCode());
    }

    @Test
    void sameKeyShouldBeDeterministic() {
        List<ExtAbility> candidates = Arrays.asList(grayExt, stableExt);
        GraySelector selector = new GraySelector(50);

        FlexPointContext.current().uid("determinism-key");
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
