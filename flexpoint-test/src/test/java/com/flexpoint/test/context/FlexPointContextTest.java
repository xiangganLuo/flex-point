package com.flexpoint.test.context;

import com.flexpoint.core.context.FlexPointContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 标准请求上下文 FlexPointContext 单测。
 *
 * @author xiangganluo
 */
public class FlexPointContextTest {

    @AfterEach
    void tearDown() {
        FlexPointContext.clear();
    }

    @Test
    void current_creates_and_binds_thread_local() {
        Assertions.assertFalse(FlexPointContext.peek().isPresent());
        FlexPointContext ctx = FlexPointContext.current();
        Assertions.assertNotNull(ctx);
        // 再次获取应为同一实例
        Assertions.assertSame(ctx, FlexPointContext.current());
        Assertions.assertTrue(FlexPointContext.peek().isPresent());
    }

    @Test
    void standard_fields_and_labels_and_attributes() {
        FlexPointContext ctx = FlexPointContext.current()
                .tenantId("t1").appCode("mall").version("2.0.0").uid("u9")
                .label("region", "cn")
                .attr("traceId", 12345L);

        Assertions.assertEquals("t1", ctx.getTenantId());
        Assertions.assertEquals("mall", ctx.getAppCode());
        Assertions.assertEquals("2.0.0", ctx.getVersion());
        Assertions.assertEquals("u9", ctx.getUid());
        Assertions.assertEquals("cn", ctx.getLabel("region"));
        Assertions.assertEquals(1, ctx.getLabels().size());
        Assertions.assertEquals(Long.valueOf(12345L), ctx.getAttr("traceId"));
    }

    @Test
    void clear_removes_context() {
        FlexPointContext.current().tenantId("t1");
        FlexPointContext.clear();
        Assertions.assertFalse(FlexPointContext.peek().isPresent());
        // clear 后 current 重新创建空上下文
        Assertions.assertNull(FlexPointContext.current().getTenantId());
    }

    @Test
    void set_binds_given_context() {
        FlexPointContext custom = new FlexPointContext().tenantId("custom");
        FlexPointContext.set(custom);
        Assertions.assertSame(custom, FlexPointContext.current());
        Assertions.assertEquals("custom", FlexPointContext.current().getTenantId());
    }
}
