package com.flexpoint.plugin.selector.tag;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.ExtTags;
import com.flexpoint.core.selector.SelectionResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TagSelector} 单元测试。
 *
 * @author xiangganluo
 */
class TagSelectorTest {

    /** 业务方 LabelResolver 由测试注入的可变 labels 提供。 */
    private final Map<String, String> labels = new HashMap<>();
    private final TagSelector selector = new TagSelector(() -> labels);

    @AfterEach
    void tearDown() {
        labels.clear();
    }

    /** 简单扩展点：仅承载 code 与 tags。 */
    static class TagExt implements ExtAbility {
        private final String code;
        private final ExtTags tags;

        TagExt(String code, ExtTags tags) {
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

    @Test
    void shouldHitWhenAllLabelsMatch() {
        TagExt cn = new TagExt("cn", ExtTags.builder().set("region", "cn").set("env", "prod").build());
        TagExt us = new TagExt("us", ExtTags.builder().set("region", "us").set("env", "prod").build());
        List<ExtAbility> candidates = Arrays.asList(cn, us);

        labels.put("region", "cn");
        labels.put("env", "prod");
        SelectionResult<ExtAbility> result = selector.select(candidates);

        assertTrue(result.isHit());
        assertEquals("cn", result.getSelected().getCode());
    }

    @Test
    void shouldMissWhenNoCandidateMatchesAllLabels() {
        TagExt cn = new TagExt("cn", ExtTags.builder().set("region", "cn").build());
        List<ExtAbility> candidates = Arrays.asList((ExtAbility) cn);

        labels.put("region", "cn");
        labels.put("env", "prod");
        SelectionResult<ExtAbility> result = selector.select(candidates);

        assertTrue(result.isMiss());
    }

    @Test
    void shouldMissWhenNoLabels() {
        TagExt cn = new TagExt("cn", ExtTags.builder().set("region", "cn").build());
        List<ExtAbility> candidates = Arrays.asList((ExtAbility) cn);

        SelectionResult<ExtAbility> result = selector.select(candidates);

        assertTrue(result.isMiss());
        assertFalse(result.isHit());
    }

    @Test
    void shouldBeAmbiguousWhenMultipleMatch() {
        TagExt a = new TagExt("a", ExtTags.builder().set("region", "cn").build());
        TagExt b = new TagExt("b", ExtTags.builder().set("region", "cn").build());
        List<ExtAbility> candidates = Arrays.asList(a, b);

        labels.put("region", "cn");
        SelectionResult<ExtAbility> result = selector.select(candidates);

        assertTrue(result.isAmbiguous());
    }
}
