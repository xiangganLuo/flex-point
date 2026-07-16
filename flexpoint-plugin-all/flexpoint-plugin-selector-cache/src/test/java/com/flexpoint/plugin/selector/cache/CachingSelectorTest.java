package com.flexpoint.plugin.selector.cache;

import com.flexpoint.core.context.FlexPointContext;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.ExtTags;
import com.flexpoint.core.selector.SelectionResult;
import com.flexpoint.core.selector.Selector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link CachingSelector} 单元测试。
 *
 * @author xiangganluo
 */
class CachingSelectorTest {

    @AfterEach
    void tearDown() {
        FlexPointContext.clear();
    }

    static class SimpleExt implements ExtAbility {
        private final String code;

        SimpleExt(String code) {
            this.code = code;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public ExtTags getTags() {
            return ExtTags.empty();
        }

        @Override
        public String getExtId() {
            return code;
        }
    }

    /** 计数 delegate：每次 select 计数并选中第一个候选。 */
    static class CountingSelector implements Selector {
        final AtomicInteger calls = new AtomicInteger();

        @Override
        public String getName() {
            return "counting";
        }

        @Override
        public <T extends ExtAbility> SelectionResult<T> select(List<T> candidates) {
            calls.incrementAndGet();
            T picked = (candidates == null || candidates.isEmpty()) ? null : candidates.get(0);
            return SelectionResult.of(getName(), candidates, picked);
        }
    }

    private final SimpleExt a = new SimpleExt("a");
    private final SimpleExt b = new SimpleExt("b");

    @Test
    void secondCallHitsCacheAndDelegateCalledOnce() {
        CountingSelector delegate = new CountingSelector();
        CachingSelector caching = new CachingSelector(delegate);
        List<ExtAbility> candidates = Arrays.asList(a, b);

        FlexPointContext.current().setUid("u1");
        SelectionResult<ExtAbility> first = caching.select(candidates);
        SelectionResult<ExtAbility> second = caching.select(candidates);

        assertTrue(first.isHit());
        assertEquals("a", second.getSelected().getCode());
        assertEquals(1, delegate.calls.get(), "delegate 应只被调用一次");
    }

    @Test
    void differentContextProducesDifferentCacheEntries() {
        CountingSelector delegate = new CountingSelector();
        CachingSelector caching = new CachingSelector(delegate);
        List<ExtAbility> candidates = Arrays.asList(a, b);

        FlexPointContext.current().setUid("u1");
        caching.select(candidates);
        FlexPointContext.current().setUid("u2");
        caching.select(candidates);

        assertEquals(2, delegate.calls.get(), "不同上下文应各自触发 delegate");
        assertEquals(2, caching.cacheSize());
    }

    @Test
    void nameDefaultsToDelegateName() {
        CachingSelector caching = new CachingSelector(new CountingSelector());
        assertEquals("counting", caching.getName());
    }

    @Test
    void customNameIsUsed() {
        CachingSelector caching = new CachingSelector(new CountingSelector(), 0L, "myCache");
        assertEquals("myCache", caching.getName());
    }

    @Test
    void expiredEntryTriggersDelegateAgain() throws InterruptedException {
        CountingSelector delegate = new CountingSelector();
        CachingSelector caching = new CachingSelector(delegate, 30L);
        List<ExtAbility> candidates = Arrays.asList(a, b);

        FlexPointContext.current().setUid("u1");
        caching.select(candidates);
        Thread.sleep(60L);
        caching.select(candidates);

        assertEquals(2, delegate.calls.get(), "TTL 过期后应重新调用 delegate");
    }
}
