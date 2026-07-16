package com.flexpoint.core.plugin.manage;

import com.flexpoint.core.plugin.Plugin;
import com.flexpoint.core.plugin.PluginDependency;
import com.flexpoint.core.plugin.PluginDescriptor;
import com.flexpoint.core.plugin.exception.PluginDependencyException;

import java.util.*;

/**
 * 依赖解析与拓扑排序（Kahn 算法）。
 *
 * <p>
 * 输入：插件集合 P，每个插件 d = p.getDescriptor() 声明 0..n 个依赖（depId）。
 * 过程：
 *   1) 为每个插件分配一个顶点，构建图 G；
 *   2) 对每个依赖关系 depId -> id 建立有向边；
 *   3) 计算入度 indegree；
 *   4) 将入度为 0 的顶点放入按 order 排序的优先队列，逐步弹出并“移除”其边，
 *      将新入度为 0 的顶点继续放入优先队列；
 *   5) 若输出数量 != P.size()，存在环。
 *
 * <p>关于 order：order 仅作为“同时就绪（无相互依赖约束）”节点之间的排序权重，
 * 通过在 Kahn 主循环中使用优先队列实现——即在满足拓扑约束的前提下，
 * 优先弹出 order 更小的节点。<b>不能</b>在拓扑排序完成后对整体再按 order 排序，
 * 否则当“被依赖者的 order 大于依赖者”时会破坏依赖顺序
 * （例如 dep(order=100) 被 main(order=1) 依赖，整体重排后会得到 [main, dep]，
 * 导致 main 先于 dep 装配）。order 相同时按 pluginId 字典序兜底，保证确定性。</p>
 *
 * 数据示例：
 * <pre>
 *   A(order=0, deps=[])
 *   B(order=1, deps=[A])     // A -> B
 *   C(order=0, deps=[A])     // A -> C
 *   D(order=5, deps=[B,C])   // B -> D, C -> D
 *   解析顺序：A, C, B, D（拓扑约束内按 order 优先）
 * </pre>
 * </p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
final class DependencyResolver {

    /**
     * 解析插件依赖顺序，返回按依赖关系排序的插件列表。
     * <p>
     * 使用 Kahn 算法进行拓扑排序，确保依赖的插件先于依赖方被加载。
     * </p>
     *
     * @param plugins 待排序的插件集合
     * @return 按依赖顺序排列的插件列表
     * @throws PluginDependencyException 当存在重复插件 ID、缺失依赖或循环依赖时抛出
     */
    static List<Plugin> resolveOrder(Collection<Plugin> plugins) {
        // ==================== 步骤 1: 构建插件 ID 映射，检测重复插件 ====================
        Map<String, Plugin> byId = new HashMap<>();
        for (Plugin p : plugins) {
            PluginDescriptor d = p.getDescriptor();
            // putIfAbsent: 如果 key 不存在则放入，返回 null；如果已存在则返回原值
            if (byId.putIfAbsent(d.getPluginId(), p) != null) {
                // 发现重复的插件 ID，直接抛出异常
                throw new PluginDependencyException("Duplicate pluginId: " + d.getPluginId());
            }
        }

        // ==================== 步骤 2: 构建有向图（邻接表）和入度表 ====================
        // edges: 邻接表，记录从每个节点出发的边（from -> to）
        Map<String, Set<String>> edges = new HashMap<>();
        // indegree: 入度表，记录每个节点的入度（有多少个其他节点指向它）
        Map<String, Integer> indegree = new HashMap<>();
        
        // 初始化所有顶点的边集和入度
        for (String id : byId.keySet()) { 
            indegree.put(id, 0);  // 初始入度为 0
            edges.put(id, new HashSet<String>());  // 初始边集为空
        }

        // 遍历所有插件，根据依赖关系建立有向边
        for (Plugin p : plugins) {
            PluginDescriptor d = p.getDescriptor();
            // 遍历当前插件的所有依赖项
            for (PluginDependency dep : d.getDependencies()) {
                String depId = dep.getPluginId();
                // 检查依赖的插件是否存在
                if (!byId.containsKey(depId)) {
                    // 依赖的插件不在插件集合中，抛出缺失依赖异常
                    throw new PluginDependencyException(
                        "Missing dependency: " + d.getPluginId() + " -> " + depId);
                }
                // 建立有向边：depId -> currentId（被依赖者指向依赖者）
                // 含义：depId 完成后才能处理 currentId
                if (edges.get(depId).add(d.getPluginId())) {
                    // 添加边成功（避免重复边），将 currentId 的入度 +1
                    indegree.put(d.getPluginId(), indegree.get(d.getPluginId()) + 1);
                }
            }
        }

        // ==================== 步骤 3: Kahn 算法核心 - 拓扑排序 ====================
        // 就绪集合使用优先队列：在满足拓扑约束（入度为 0）的前提下，
        // 优先弹出 order 更小的节点；order 相同按 pluginId 字典序兜底，确保确定性。
        // 注意：order 只能在此处作为“就绪节点之间”的权重，绝不能在排序完成后对整体再排序，
        // 否则会破坏“被依赖者 order 大于依赖者”场景下的依赖顺序。
        PriorityQueue<String> ready = new PriorityQueue<>((a, b) -> {
            int oa = byId.get(a).getDescriptor().getOrder();
            int ob = byId.get(b).getDescriptor().getOrder();
            if (oa != ob) {
                return Integer.compare(oa, ob);
            }
            return a.compareTo(b);
        });
        for (String id : byId.keySet()) {
            if (indegree.get(id) == 0) {  // 入度为 0，说明没有前置依赖
                ready.add(id);
            }
        }

        // 存储拓扑排序结果的列表
        List<Plugin> ordered = new ArrayList<>();

        // 开始拓扑排序：不断从就绪队列中取出 order 最小的顶点并"移除"其出边
        while (!ready.isEmpty()) {
            String id = ready.poll();
            // 将该顶点加入结果序列
            ordered.add(byId.get(id));

            // "移除"该顶点的所有出边，即将其所有后继节点的入度减 1
            for (String to : edges.get(id)) {
                // 将从 id 出发的边的目标节点入度减 1
                indegree.put(to, indegree.get(to) - 1);
                // 如果目标节点的入度变为 0，说明其所有前置依赖都已处理完成
                if (indegree.get(to) == 0) {
                    ready.add(to);  // 将新入度为 0 的节点放入就绪队列
                }
            }
        }

        // ==================== 步骤 4: 检测循环依赖 ====================
        // 如果结果数量不等于插件总数，说明存在循环依赖
        // 原因：环中的节点入度永远无法降为 0，永远不会被加入结果集
        if (ordered.size() != plugins.size()) {
            throw new PluginDependencyException("Cyclic dependency detected");
        }

        return ordered;
    }
}
