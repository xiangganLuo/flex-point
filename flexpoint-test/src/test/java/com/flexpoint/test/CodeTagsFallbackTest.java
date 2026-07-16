package com.flexpoint.test;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.ExtTags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * code + tags → code 回退语义用例（specific → general）。
 *
 * @author xiangganluo
 */
public class CodeTagsFallbackTest {

    @FpSelector("fb")
    interface FbAbility extends ExtAbility {
        String who();
    }

    /** code=a，带 version=2 标签。 */
    static class TaggedV2 implements FbAbility {
        @Override public String getCode() { return "a"; }
        @Override public ExtTags getTags() { return ExtTags.builder().set("version", "2").build(); }
        @Override public String who() { return "v2"; }
    }

    /** code=a，无标签（作为回退目标）。 */
    static class Plain implements FbAbility {
        @Override public String getCode() { return "a"; }
        @Override public String who() { return "plain"; }
    }

    private FlexPoint fp;

    @BeforeEach
    void setup() {
        fp = FlexPointBuilder.create().build();
        fp.register(new Plain());
        fp.register(new TaggedV2());
    }

    @Test
    void precise_code_and_tags_match_wins() {
        FbAbility a = fp.findAbilityByCodeAndTagsOrFallback(FbAbility.class, "a", "version", "2");
        Assertions.assertNotNull(a);
        Assertions.assertEquals("v2", a.who(), "应命中 code+tags 精确匹配");
    }

    @Test
    void falls_back_to_code_only_when_tags_miss() {
        FbAbility a = fp.findAbilityByCodeAndTagsOrFallback(FbAbility.class, "a", "version", "9");
        Assertions.assertNotNull(a, "tags 未命中应回退到 code-only");
        Assertions.assertEquals("a", a.getCode());
    }

    @Test
    void returns_null_when_code_also_absent() {
        FbAbility a = fp.findAbilityByCodeAndTagsOrFallback(FbAbility.class, "zzz", "version", "2");
        Assertions.assertNull(a, "code 与 code+tags 均未命中应返回 null");
    }
}
