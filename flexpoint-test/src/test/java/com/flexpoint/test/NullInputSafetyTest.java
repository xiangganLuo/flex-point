package com.flexpoint.test;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.ext.ExtAbility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 修复 6 回归：code/标签为 null 时的输入校验，均不应 NPE。
 *
 * @author xiangganluo
 */
public class NullInputSafetyTest {

    @FpSelector("noopSelector")
    interface DemoAbility extends ExtAbility {
        String exec();
    }

    static class DemoAbilityImpl implements DemoAbility {
        @Override public String getCode() { return "a"; }
        @Override public String exec() { return "done"; }
    }

    private FlexPoint build() {
        FlexPoint fp = FlexPointBuilder.create().build();
        fp.register(new DemoAbilityImpl());
        return fp;
    }

    @Test
    void findByCode_null_code_returns_empty_without_npe() {
        FlexPoint fp = build();
        List<DemoAbility> matched = Assertions.assertDoesNotThrow(
                () -> fp.findAbilitysByCode(DemoAbility.class, null));
        Assertions.assertTrue(matched.isEmpty());
        fp.shutdown();
    }

    @Test
    void findByCodeAndTags_null_tag_key_is_skipped_without_npe() {
        FlexPoint fp = build();
        // null 标签 key 应被跳过，退化为仅按 code 匹配 -> 命中
        List<DemoAbility> matched = Assertions.assertDoesNotThrow(
                () -> fp.findAbilitysByCodeAndTags(DemoAbility.class, "a", null, "v"));
        Assertions.assertEquals(1, matched.size());
        fp.shutdown();
    }

    @Test
    void findByCodeAndTags_null_tag_value_matches_absent_tag_without_npe() {
        FlexPoint fp = build();
        // 标签值为 null，扩展点无该标签(取值 null)，Objects.equals(null,null)=true -> 命中，且不 NPE
        List<DemoAbility> matched = Assertions.assertDoesNotThrow(
                () -> fp.findAbilitysByCodeAndTags(DemoAbility.class, "a", "k", null));
        Assertions.assertEquals(1, matched.size());
        fp.shutdown();
    }

    @Test
    void findByCode_null_code_and_null_ext_code_still_safe() {
        FlexPoint fp = build();
        // 双 null（code=null）不 NPE
        Assertions.assertDoesNotThrow(() -> fp.findAbilityByCode(DemoAbility.class, null));
        fp.shutdown();
    }
}
