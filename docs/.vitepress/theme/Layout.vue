<script setup lang="ts">
import DefaultTheme from 'vitepress/theme'
import { useRoute } from 'vitepress'
import { nextTick, onMounted, watch } from 'vue'

const { Layout } = DefaultTheme
const route = useRoute()

// 首页特性卡片：滚动进入时淡入上移（IntersectionObserver）。
// SSR 安全；prefers-reduced-motion 下直接显形，不做动画。
function setupReveal() {
  if (typeof window === 'undefined') return
  const targets = document.querySelectorAll<HTMLElement>('.VPHome .VPFeature')
  if (!targets.length) return

  const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (reduce) {
    targets.forEach((el) => el.classList.add('fp-in'))
    return
  }

  const io = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('fp-in')
          io.unobserve(entry.target)
        }
      })
    },
    { threshold: 0.12, rootMargin: '0px 0px -8% 0px' }
  )

  targets.forEach((el, i) => {
    el.style.setProperty('--fp-delay', `${(i % 6) * 70}ms`)
    io.observe(el)
  })
}

onMounted(() => nextTick(setupReveal))
watch(() => route.path, () => nextTick(setupReveal))
</script>

<template>
  <Layout />
</template>
