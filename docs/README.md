# Flex Point 官网与文档

基于 [VitePress](https://vitepress.dev/) 构建的 Flex Point 官方站点（首页 + 文档），Markdown 驱动、支持本地全文搜索与 Mermaid 图表。

## 环境要求

- Node.js **18+**（推荐 20 LTS）
- npm（或 pnpm / yarn）

## 本地开发

```bash
# 1. 安装依赖（在 docs/ 目录下执行）
npm install

# 2. 启动开发服务器（热更新，默认 http://localhost:5173/）
npm run docs:dev
```

## 构建与预览

```bash
# 构建静态站点，产物输出到 .vitepress/dist/
npm run docs:build

# 本地预览构建产物
npm run docs:preview
```

构建产物 `.vitepress/dist/` 已在 `.gitignore` 中忽略，不纳入版本库。

## 目录结构

```
docs/
├─ package.json               # 依赖与脚本（docs:dev / docs:build / docs:preview）
├─ index.md                   # 首页（layout: home 落地页）
├─ public/
│  └─ logo.svg                # 站点 Logo / favicon
├─ guide/                     # 文档正文
│  ├─ introduction.md         # 简介
│  ├─ quickstart.md           # 快速开始
│  ├─ concepts.md             # 核心概念
│  ├─ selector.md             # 选择器与决策解释
│  ├─ interceptor.md          # 调用管线与拦截器
│  ├─ monitor.md              # 监控与可观测
│  ├─ plugin.md               # 插件体系（Plugin SPI）
│  ├─ plugins-official.md     # 官方插件模块
│  └─ springboot.md           # Spring Boot 接入
└─ .vitepress/
   ├─ config.mts              # 站点配置（导航、侧边栏、搜索、mermaid）
   └─ theme/
      ├─ index.ts             # 主题入口
      └─ custom.css           # 品牌色（#2563eb）与首页样式
```

## 部署

`npm run docs:build` 产出的 `.vitepress/dist/` 为纯静态资源，可托管到任意静态服务器 / CDN / GitHub Pages。

- 若部署到 **自定义域名或根路径**，保持 `.vitepress/config.mts` 中 `base: '/'`。
- 若部署到 **GitHub Pages 项目页**（如 `https://<user>.github.io/flex-point/`），将 `base` 改为 `'/flex-point/'`。

## 写作约定

- 代码示例统一使用 ```` ```java ```` / ```` ```xml ```` / ```` ```yaml ```` 等围栏。
- 流程 / 架构图使用 ```` ```mermaid ```` 围栏（由 `vitepress-plugin-mermaid` 渲染）。
- 提示框使用 VitePress 容器：`::: tip` / `::: info` / `::: warning` / `::: danger`。
- 版本号统一 `2.0.0`，正文以中文为主。
