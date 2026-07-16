package com.flexpoint.plugin.selector.weight;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.ExtTags;
import com.flexpoint.core.selector.SelectionResult;
import com.flexpoint.core.selector.Selector;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

/**
 * 官方插件：权重选择器。
 *
 * <p>按候选扩展点 tag {@code weight}（默认 1）做加权随机选择，从多个候选里选出一个。
 * 因此直接实现 {@link Selector} 并用 {@link SelectionResult#of} 返回，而非走
 * {@code AbstractSelector} 的「多命中=歧义」语义。</p>
 *
 * <p>随机源可注入以便测试：默认 {@link Random}，也可传入固定 seed 或自定义 {@link Random}。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public class WeightSelector implements Selector {

    /** 选择器名称。 */
    public static final String NAME = "weightSelector";

    /** 权重标签键。 */
    public static final String WEIGHT_TAG = "weight";

    private final Random random;

    /** 使用默认随机源。 */
    public WeightSelector() {
        this(new Random());
    }

    /** 使用固定 seed，便于可复现的确定性测试。 */
    public WeightSelector(long seed) {
        this(new Random(seed));
    }

    /** 注入自定义随机源。 */
    public WeightSelector(Random random) {
        this.random = random != null ? random : new Random();
    }

    @Override
    public <T extends ExtAbility> SelectionResult<T> select(List<T> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return SelectionResult.of(NAME, candidates, null);
        }
        int totalWeight = 0;
        for (T ext : candidates) {
            totalWeight += weightOf(ext);
        }
        if (totalWeight <= 0) {
            log.debug("[{}] 所有候选权重为 0，未命中", NAME);
            return SelectionResult.of(NAME, candidates, null);
        }
        int target = random.nextInt(totalWeight);
        int cumulative = 0;
        T picked = null;
        for (T ext : candidates) {
            cumulative += weightOf(ext);
            if (target < cumulative) {
                picked = ext;
                break;
            }
        }
        log.debug("[{}] 加权随机: 总权重={}, target={}, 命中={}", NAME, totalWeight, target,
                picked == null ? "null" : picked.getExtId());
        return SelectionResult.of(NAME, candidates, picked);
    }

    /** 读取候选权重：缺省 1，负值按 0 处理（不参与抽样）。 */
    private int weightOf(ExtAbility ext) {
        ExtTags tags = ext.getTags();
        Integer w = tags == null ? null : tags.getInt(WEIGHT_TAG);
        if (w == null) {
            return 1;
        }
        return Math.max(0, w);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
