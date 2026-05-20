<template>
  <div style="padding:16px; border-bottom:1px solid var(--border-color);">
    <h2 style="margin:0; color:var(--brand-color); font-size:18px;">ClassNote</h2>
  </div>

  <div style="padding:12px 16px;">
    <el-input v-model="searchKeyword" placeholder="搜索笔记..." size="small" clearable
      @keyup.enter="goSearch" @clear="goSearch">
      <template #prefix><el-icon><Search /></el-icon></template>
    </el-input>
  </div>

  <el-menu :default-active="activeMenu" router style="border-right:none; flex:1; overflow-y:auto;">
    <el-menu-item index="/notes">
      <el-icon><Notebook /></el-icon>
      <span>全部笔记</span>
    </el-menu-item>

    <el-sub-menu index="notebooks">
      <template #title>
        <el-icon><Folder /></el-icon>
        <span>笔记本</span>
      </template>
      <el-menu-item v-for="nb in notebookStore.notebooks" :key="nb.id"
        :index="`/notes?notebookId=${nb.id}`">
        {{ nb.name }}
        <span style="color:var(--text-secondary); font-size:12px; margin-left:auto;">({{ nb.noteCount }})</span>
      </el-menu-item>
    </el-sub-menu>

    <el-sub-menu index="tags-menu">
      <template #title>
        <el-icon><PriceTag /></el-icon>
        <span>标签</span>
      </template>
      <el-menu-item v-for="tag in tagStore.tags" :key="tag.id"
        :index="`/notes?tagId=${tag.id}`">
        <el-tag :color="tag.color" size="small" style="color:#fff;">{{ tag.name }}</el-tag>
      </el-menu-item>
    </el-sub-menu>

    <el-menu-item index="/analyzer">
      <el-icon><DataAnalysis /></el-icon>
      <span>项目分析</span>
    </el-menu-item>

    <el-menu-item index="/settings">
      <el-icon><Tools /></el-icon>
      <span>设置</span>
    </el-menu-item>

    <el-menu-item index="/tags">
      <el-icon><Setting /></el-icon>
      <span>管理标签</span>
    </el-menu-item>
  </el-menu>

  <div style="padding:12px 16px; border-top:1px solid var(--border-color);">
    <el-button @click="prefStore.toggleTheme()" size="small" style="width:100%;">
      <el-icon><component :is="prefStore.theme==='dark'?'Sunny':'Moon'" /></el-icon>
      {{ prefStore.theme === 'dark' ? '浅色模式' : '深色模式' }}
    </el-button>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useNotebookStore } from '../stores/notebook'
import { useTagStore } from '../stores/tag'
import { usePreferenceStore } from '../stores/preference'

export default {
  name: 'Sidebar',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const notebookStore = useNotebookStore()
    const tagStore = useTagStore()
    const prefStore = usePreferenceStore()
    const searchKeyword = ref(route.query.keyword || '')

    const activeMenu = computed(() => {
      if (route.path.startsWith('/analyzer')) return '/analyzer'
      if (route.path.startsWith('/settings')) return '/settings'
      if (route.path.startsWith('/tags')) return '/tags'
      if (route.path.startsWith('/search')) return ''
      return '/notes'
    })

    const goSearch = () => {
      if (searchKeyword.value) {
        router.push({ name: 'SearchView', query: { keyword: searchKeyword.value } })
      }
    }

    onMounted(async () => {
      await Promise.all([
        notebookStore.fetchNotebooks({ size: 50 }),
        tagStore.fetchTags(),
        prefStore.fetch()
      ])
      prefStore.applyTheme()
    })

    return { notebookStore, tagStore, prefStore, searchKeyword, activeMenu, goSearch }
  }
}
</script>
