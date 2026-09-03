<template>
  <div class="max-w-3xl mx-auto px-6 py-16 md:py-24 text-neutral-900">
    <!-- 头像（可选：没设 portrait 就不显示，不会裂图） -->
    <img
        v-if="site.portrait"
        :src="resolveAsset(site.portrait)"
        alt="作者头像"
        class="w-20 h-20 rounded-2xl object-cover grayscale hover:grayscale-0 transition-all duration-700 mb-8"
    >

    <!-- 头部 -->
    <header class="mb-12">
      <p class="text-xs font-medium uppercase tracking-widest text-neutral-400 mb-4">关于我</p>
      <h1 class="text-4xl md:text-5xl font-semibold tracking-tight text-neutral-900">{{ profile.name }}</h1>
      <p v-if="profile.identity" class="mt-3 text-neutral-500 font-light">{{ profile.identity }}</p>
      <p v-if="profile.motto" class="mt-6 text-neutral-400 italic font-light text-[15px]">
        「{{ profile.motto }}」
      </p>
    </header>

    <!-- 自我介绍正文 -->
    <section v-if="profile.bio.length" class="space-y-4 text-[15px] md:text-base text-neutral-600 font-light leading-relaxed">
      <p v-for="(paragraph, i) in profile.bio" :key="'bio-' + i">{{ paragraph }}</p>
    </section>

    <!-- 技术栈 -->
    <section v-if="profile.skills.length" class="mt-12">
      <h2 class="text-xs font-semibold uppercase tracking-widest text-neutral-400 mb-5">技术栈</h2>
      <ul class="space-y-3">
        <li
            v-for="(item, i) in profile.skills"
            :key="'skill-' + i"
            class="flex flex-col sm:flex-row sm:items-baseline gap-1 sm:gap-4"
        >
          <span class="text-sm font-medium text-neutral-800 sm:w-40 shrink-0">{{ item.label }}</span>
          <span v-if="item.desc" class="text-sm text-neutral-500 font-light">{{ item.desc }}</span>
        </li>
      </ul>
    </section>

    <!-- 兴趣 / 爱好 -->
    <section v-if="profile.interests.length" class="mt-12">
      <h2 class="text-xs font-semibold uppercase tracking-widest text-neutral-400 mb-5">兴趣与爱好</h2>
      <div class="flex flex-wrap gap-2">
        <span
            v-for="(item, i) in profile.interests"
            :key="'interest-' + i"
            class="px-3 py-1.5 rounded-full border border-neutral-200 text-sm text-neutral-600"
        >{{ item }}</span>
      </div>
    </section>

    <!-- 喜欢的作品 -->
    <section v-if="profile.favorites.length" class="mt-12">
      <h2 class="text-xs font-semibold uppercase tracking-widest text-neutral-400 mb-5">喜欢的作品</h2>
      <ul class="space-y-2.5 text-[15px] text-neutral-600 font-light">
        <li v-for="(item, i) in profile.favorites" :key="'fav-' + i" class="flex items-baseline gap-3">
          <span class="text-neutral-300">·</span>
          <span>{{ item }}</span>
        </li>
      </ul>
    </section>

    <!-- 经历 / 时间线 -->
    <section v-if="profile.journey.length" class="mt-12">
      <h2 class="text-xs font-semibold uppercase tracking-widest text-neutral-400 mb-5">经历</h2>
      <ol class="border-l border-neutral-200 ml-1.5 space-y-6">
        <li v-for="(item, i) in profile.journey" :key="'journey-' + i" class="pl-6 relative">
          <span class="absolute left-0 top-1.5 -translate-x-1/2 w-2 h-2 rounded-full bg-neutral-900"></span>
          <span v-if="item.period" class="block text-xs font-medium text-neutral-400 uppercase tracking-wider">{{ item.period }}</span>
          <p class="mt-1 text-sm text-neutral-600 font-light leading-relaxed">{{ item.text }}</p>
        </li>
      </ol>
    </section>

    <!-- 找到我 -->
    <section class="mt-14 pt-10 border-t border-neutral-100">
      <h2 class="text-xs font-semibold uppercase tracking-widest text-neutral-400 mb-5">找到我</h2>
      <div class="flex flex-wrap items-center gap-3">
        <a
            v-if="socials.github"
            :href="socials.github"
            target="_blank"
            rel="noopener noreferrer"
            class="inline-flex items-center gap-2 h-11 px-5 rounded-full border border-neutral-200 text-sm text-neutral-600 hover:text-neutral-900 hover:border-neutral-400 transition-all duration-300"
        >
          <icon-github :width="16" /> GitHub
        </a>
        <a
            v-if="socials.bilibili"
            :href="socials.bilibili"
            target="_blank"
            rel="noopener noreferrer"
            class="inline-flex items-center gap-2 h-11 px-5 rounded-full border border-neutral-200 text-sm text-neutral-600 hover:text-neutral-900 hover:border-neutral-400 transition-all duration-300"
        >
          <icon-bilibili :width="16" /> Bilibili
        </a>
        <a
            v-if="socials.email"
            :href="'mailto:' + socials.email"
            class="inline-flex items-center gap-2 h-11 px-5 rounded-full border border-neutral-200 text-sm text-neutral-600 hover:text-neutral-900 hover:border-neutral-400 transition-all duration-300"
        >
          <icon-mail :width="16" /> {{ socials.email }}
        </a>
      </div>
      <p v-if="socials.qq" class="mt-5 text-sm text-neutral-400">QQ：{{ socials.qq }}</p>
    </section>

    <div class="mt-14">
      <router-link to="/" class="inline-flex items-center gap-1.5 text-sm text-neutral-400 hover:text-neutral-900 transition-colors">
        返回首页
        <icon-arrow-right :width="14" />
      </router-link>
    </div>
  </div>
</template>

<script>
import { SITE } from '@/config/site'
import { resolveAsset } from '@/utils/request'

export default {
  name: 'AboutMe',
  data() {
    return {
      site: SITE
    }
  },
  computed: {
    profile() {
      return this.site.profile
    },
    socials() {
      return this.site.socials
    }
  },
  methods: {
    resolveAsset
  }
}
</script>

<style scoped>
/* 极简风格，样式主要走 Tailwind；这里只留少量兜底 */
</style>
