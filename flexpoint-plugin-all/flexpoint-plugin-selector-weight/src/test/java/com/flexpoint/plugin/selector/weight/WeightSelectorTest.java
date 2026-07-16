package com.flexpoint.plugin.selector.weight;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.ExtTags;
import com.flexpoint.core.selector.SelectionResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link WeightSelector} 单元测试。
 *
 * @author xiangganluo
 */
class WeightSelectorTest {

    static class WeightExt implements ExtAbility {
        private final String code;
        private final ExtTags tags;

        WeightExt(String code, Integer weight) {
            this.code = code;
            this.tags = weight == null
                    ? ExtTags.empty()
                    : ExtTags.builder().set("weight", weight).build();
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

    @Test
    void fixedSeedIsDeterministic() {
        List<ExtAbility> candidates = Arrays.asList(
                new WeightExt("a", 1), new WeightExt("b", 1), new WeightExt("c", 1));

        String pick1 = new WeightSelector(123L).select(candidates).getSelected().getCode();
        String pick2 = new WeightSelector(123L).select(candidates).getSelected().getCode();

        assertEquals(pick1, pick2);
    }

    @Test
    void zeroWeightCandidateIsNeverSelected() {
        WeightExt heavy = new WeightExt("heavy", 10);
        WeightExt zero = new WeightExt("zero", 0);
        List<ExtAbility> candidates = Arrays.asList(heavy, zero);
        WeightSelector selector = new WeightSelector(7L);

        for (int i = 0; i < 500; i++) {
            SelectionResult<ExtAbility> result = selector.select(candidates);
            assertTrue(result.isHit());
            assertNotEquals("zero", result.getSelected().getCode());
        }
    }

    @Test
    void allZeroWeightsResultInMiss() {
        List<ExtAbility> candidates = Arrays.asList(
                new WeightExt("a", 0), new WeightExt("b", 0));

        SelectionResult<ExtAbility> result = new WeightSelector(1L).select(candidates);

        assertTrue(result.isMiss());
    }

    @Test
    void missingWeightDefaultsToOneAndIsSelectable() {
        WeightExt onlyDefault = new WeightExt("default", null);
        List<ExtAbility> candidates = Collections.singletonList((ExtAbility) onlyDefault);

        SelectionResult<ExtAbility> result = new WeightSelector(1L).select(candidates);

        assertTrue(result.isHit());
        assertEquals("default", result.getSelected().getCode());
    }

    @Test
    void emptyCandidatesResultInMiss() {
        SelectionResult<ExtAbility> result =
                new WeightSelector(1L).select(Collections.<ExtAbility>emptyList());

        assertTrue(result.isMiss());
    }

    @Test
    void higherWeightIsSelectedMoreOften() {
        WeightExt heavy = new WeightExt("heavy", 9);
        WeightExt light = new WeightExt("light", 1);
        List<ExtAbility> candidates = Arrays.asList(heavy, light);
        WeightSelector selector = new WeightSelector(2024L);

        int heavyCount = 0;
        int rounds = 2000;
        for (int i = 0; i < rounds; i++) {
            if ("heavy".equals(selector.select(candidates).getSelected().getCode())) {
                heavyCount++;
            }
        }
        // 期望约 90%，宽松断言 heavy 明显多于 light
        assertTrue(heavyCount > rounds * 0.75, "heavyCount=" + heavyCount);
    }
}
