# 贡献指南

如果你觉得 Flex Point 有优化空间，或有更好的设计思路，欢迎随时提交 PR（Pull Request）。我们鼓励社区共同完善、壮大本项目。

无论是修复 Bug、补充文档、新增官方插件，还是改进内核机制，都非常欢迎。

## 贡献代码的步骤

1. 在 GitHub 上 **fork** [本项目](https://github.com/xiangganLuo/flex-point) 到你的个人仓库。
2. 将 fork 后的仓库 **clone** 到本地。
3. 在本地新建分支进行代码修改和优化（建议一个 PR 只做一件事）。
4. **commit** 并 **push** 到你的远程仓库。
5. 登录 GitHub，在你的仓库首页点击 **Pull Request** 按钮，填写说明信息后提交。
6. 等待维护者 **review** 并合并。

```bash
# 1. clone 你 fork 后的仓库
git clone https://github.com/<your-name>/flex-point.git
cd flex-point

# 2. 新建特性分支
git checkout -b feat/my-improvement

# 3. 修改后提交并推送
git add .
git commit -m "feat(core): 支持自定义选择器优先级"
git push origin feat/my-improvement
```

## PR 遵循的原则

欢迎任何人为 Flex Point 添砖加瓦。为保证代码质量与可维护性，请遵循以下原则：

- **注释完备**：每个新增方法请按照 JavaDoc 规范标明方法说明、参数说明、返回值说明等；必要时补充单元测试。
- **依赖规范**：新增功能尽量避免引入额外的第三方库；内核模块（`flexpoint-core`）不得依赖 Spring。
- **风格统一**：遵循项目现有的代码风格与格式，命名、分层与既有实现保持一致。
- **测试通过**：提交前确保 `mvn clean test` 全绿；涉及行为变更时补充对应测试。

## 本地开发

### 构建与测试

框架为标准 Maven 多模块工程，本地构建与测试：

```bash
# 全反应堆编译 + 测试（本地非发布构建可跳过 GPG 签名）
mvn clean test -Dgpg.skip=true
```

### 文档预览

官网文档基于 [VitePress](https://vitepress.dev/) 构建，源码位于 `docs/`：

```bash
cd docs
npm install          # 首次安装依赖
npm run docs:dev     # 本地预览，默认 http://localhost:5173
npm run docs:build   # 生产构建（提交文档改动前请确保通过）
```

## 提交规范

项目采用[约定式提交（Conventional Commits）](https://www.conventionalcommits.org/zh-hans/)，提交信息格式为 `type(scope): 描述`：

| type | 含义 |
|------|------|
| `feat` | 新增功能 |
| `fix` | 修复缺陷 |
| `refactor` | 重构（非功能变更） |
| `docs` | 文档变更 |
| `test` | 测试相关 |
| `chore` | 构建流程、辅助工具等杂项 |

示例：`feat(selector): 新增权重选择器`、`docs(guide): 补充术语表`。

::: tip
提交前建议先跑一遍 `mvn clean test`；文档改动请确保 `npm run docs:build` 通过，避免死链。
:::

## 需要帮助？

- 遇到问题或有想法，欢迎[提交 Issue](https://github.com/xiangganLuo/flex-point/issues)。
- 也可以在[联系作者](/guide/contact)页面找到作者的联系方式。
