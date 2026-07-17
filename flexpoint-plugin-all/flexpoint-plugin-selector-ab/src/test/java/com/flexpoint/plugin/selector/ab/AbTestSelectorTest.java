package com.flexpoint.plugin.selector.ab;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.ExtTags;
import com.flexpoint.core.selector.SelectionResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AbTestSelector} 单元测试。
 *
 * @author xiangganluo
 */
class AbTestSelectorTest {

    static class AbExt implements ExtAbility {
        private final String code;
        private final ExtTags tags;

        AbExt(String code, String bucket) {
            this.code = code;
            this.tags = ExtTags.builder().set("bucket", bucket).build();
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

    private final AbExt bucketA = new AbExt("implA", "A");
    private final AbExt bucketB = new AbExt("implB", "B");

    @Test
    void allTrafficToBucketA() {
        Map<String, Integer> buckets = new HashMap<>();
        buckets.put("A", 100);
        buckets.put("B", 0);

        SelectionResult<ExtAbility> result =
                new AbTestSelector(buckets, () -> "user-1").select(Arrays.asList(bucketA, bucketB));

        assertTrue(result.isHit());
        assertEquals("implA", result.getSelected().getCode());
    }

    @Test
    void allTrafficToBucketB() {
        Map<String, Integer> buckets = new HashMap<>();
        buckets.put("A", 0);
        buckets.put("B", 100);

        SelectionResult<ExtAbility> result =
                new AbTestSelector(buckets, () -> "user-1").select(Arrays.asList(bucketA, bucketB));

        assertTrue(result.isHit());
        assertEquals("implB", result.getSelected().getCode());
    }

    @Test
    void missWhenKeyAbsent() {
        Map<String, Integer> buckets = new HashMap<>();
        buckets.put("A", 50);
        buckets.put("B", 50);

        SelectionResult<ExtAbility> result =
                new AbTestSelector(buckets, () -> null).select(Arrays.asList(bucketA, bucketB));

        assertTrue(result.isMiss());
    }

    @Test
    void splitIsDeterministicForSameKey() {
        Map<String, Integer> buckets = new HashMap<>();
        buckets.put("A", 50);
        buckets.put("B", 50);
        AbTestSelector selector = new AbTestSelector(buckets, () -> "consistent-user");
        List<ExtAbility> candidates = Arrays.asList(bucketA, bucketB);

        String firstPick = selector.select(candidates).getSelected().getCode();
        String secondPick = selector.select(candidates).getSelected().getCode();

        assertEquals(firstPick, secondPick);
    }

    @Test
    void customKeyResolverWorks() {
        Map<String, Integer> buckets = new HashMap<>();
        buckets.put("A", 100);

        SelectionResult<ExtAbility> result =
                new AbTestSelector(buckets, () -> "e-1").select(Arrays.asList(bucketA, bucketB));

        assertTrue(result.isHit());
        assertEquals("implA", result.getSelected().getCode());
    }
}
