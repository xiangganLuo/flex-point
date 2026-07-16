import { defineConfig } from 'vitepress'
import { withMermaid } from 'vitepress-plugin-mermaid'

const GITHUB = 'https://github.com/xiangganLuo/flex-point'

// https://vitepress.dev/reference/site-config
export default withMermaid(
  defineConfig({
    lang: 'zh-CN',
    title: 'Flex Point',
    description: '多场景适配、极致灵活的扩展点框架',
    lastUpdated: true,
    cleanUrls: true,
    // README.md 仅作开发说明，不作为站点页面构建
    srcExclude: ['README.md'],
    // 部署到自定义域名/根路径时保持默认 '/'；GitHub Pages 项目页可改为 '/flex-point/'
    base: '/',

    head: [
      ['link', { rel: 'icon', href: '/logo.svg' }],
      ['meta', { name: 'theme-color', content: '#2563eb' }],
      // 首帧前标记，用于首页动画的初始隐藏态；prefers-reduced-motion 下不添加（降级为静态）
      [
        'script',
        {},
        "try{if(!matchMedia('(prefers-reduced-motion: reduce)').matches){document.documentElement.classList.add('fp-anim')}}catch(e){}",
      ],
    ],

    themeConfig: {
      logo: '/logo.svg',
      siteTitle: 'Flex Point',

      nav: [
        { text: '指南', link: '/guide/introduction', activeMatch: '/guide/' },
        { text: '快速开始', link: '/guide/quickstart' },
        {
          text: '社区',
          items: [
            { text: '贡献指南', link: '/guide/contributing' },
            { text: '联系作者', link: '/guide/contact' },
          ],
        },
        {
          text: 'v2.0.0',
          items: [
            { text: '更新日志', link: `${GITHUB}/releases` },
            { text: '提交 Issue', link: `${GITHUB}/issues` },
          ],
        },
      ],

      sidebar: {
        '/guide/': [
          {
            text: '开始',
            items: [
              { text: '简介', link: '/guide/introduction' },
              { text: '快速开始', link: '/guide/quickstart' },
            ],
          },
          {
            text: '核心概念',
            items: [
              { text: '核心概念', link: '/guide/concepts' },
              { text: '扩展点', link: '/guide/ext' },
              { text: '选择器', link: '/guide/selector' },
              { text: '插件体系（Plugin SPI）', link: '/guide/plugin' },
              { text: '可观测', link: '/guide/observability' },
            ],
          },
          {
            text: '官方插件',
            items: [
              { text: '官方插件模块', link: '/guide/plugins-official' },
            ],
          },
          {
            text: '参考',
            items: [
              { text: '术语表', link: '/guide/glossary' },
            ],
          },
          {
            text: '集成',
            items: [
              { text: 'Spring Boot 接入', link: '/guide/springboot' },
            ],
          },
          {
            text: '社区',
            items: [
              { text: '贡献指南', link: '/guide/contributing' },
              { text: '联系作者', link: '/guide/contact' },
            ],
          },
        ],
      },

      socialLinks: [{ icon: 'github', link: GITHUB }],

      search: {
        provider: 'local',
        options: {
          translations: {
            button: { buttonText: '搜索文档', buttonAriaLabel: '搜索文档' },
            modal: {
              noResultsText: '无法找到相关结果',
              resetButtonTitle: '清除查询条件',
              footer: {
                selectText: '选择',
                navigateText: '切换',
                closeText: '关闭',
              },
            },
          },
        },
      },

      footer: {
        message: '基于 Apache License 2.0 发布',
        copyright: `Copyright © 2024-${new Date().getFullYear()} Flex Point`,
      },

      outline: { level: [2, 3], label: '本页目录' },
      docFooter: { prev: '上一页', next: '下一页' },
      returnToTopLabel: '回到顶部',
      sidebarMenuLabel: '菜单',
      darkModeSwitchLabel: '主题',
      lightModeSwitchTitle: '切换到浅色模式',
      darkModeSwitchTitle: '切换到深色模式',
      externalLinkIcon: true,
    },

    // 中文分词友好的锚点
    markdown: {
      lineNumbers: false,
    },

    // mermaid 主题（跟随亮/暗自动切换由插件处理）
    mermaid: {
      theme: 'default',
    },
  })
)
