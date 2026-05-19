<template>
  <div style="height:100%; display:flex; flex-direction:column; padding:20px;">
    <div style="margin-bottom:16px;">
      <el-input v-model="keyword" placeholder="输入关键词搜索..." clearable
        @keyup.enter="search" @clear="search" style="max-width:400px;">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="filterNotebookId" placeholder="限定笔记本" clearable style="width:180px; margin-left:8px;">
        <el-option v-for="nb in notebookStore.notebooks" :key="nb.id" :label="nb.name" :value="nb.id" />
      </el-select>
      <el-select v-model="filterTagId" placeholder="限定标签" clearable style="width:180px; margin-left:8px;">
        <el-option v-for="tag in tagStore.tags" :key="tag.id" :label="tag.name" :value="tag.id" />
      </el-select>
      <el-button type="primary" @click="search" style="margin-left:8px;">搜索</el-button>
    </div>

    <div v-if="results.length === 0 && searched" style="text-align:center; color:#909399; padding:60px;">
      未找到匹配的笔记
    </div>

    <div v-for="note in results" :key="note.id" style="margin-bottom:16px; cursor:pointer;"
      @click="router.push(`/notes/${note.id}/edit`)">
      <el-card shadow="hover">
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <h4 v-html="note.title" style="margin:0;"></h4>
          <span style="color:#909399; font-size:12px;">{{ note.notebookName }}</span>
        </div>
        <p v-html="note.plainText" style="margin:8px 0 0; color:#666; font-size:14px;"></p>
        <div style="margin-top:8px;">
          <el-tag v-for="tag in note.tags" :key="tag.id" :color="tag.color"
            size="small" style="color:#fff; margin-right:4px;">{{ tag.name }}</el-tag>
          <span style="color:#909399; font-size:12px; margin-left:8px;">
            {{ new Date(note.updatedAt).toLocaleString('zh-CN') }}
          </span>
        </div>
      </el-card>
    </div>

    <el-pagination v-if="total > 0" style="margin-top:auto; justify-content:center;"
      :current-page="page" :page-size="size" :total="total"
      layout="prev, pager, next" @current-change="p => { page = p; search() }" />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchApi } from '../api/search'
import { useNotebookStore } from '../stores/notebook'
import { useTagStore } from '../stores/tag'

export default {
  name: 'SearchView',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const notebookStore = useNotebookStore()
    const tagStore = useTagStore()

    const keyword = ref(route.query.keyword || '')
    const filterNotebookId = ref('')
    const filterTagId = ref('')
    const results = ref([])
    const total = ref(0)
    const page = ref(1)
    const size = 20
    const searched = ref(false)

    const search = async () => {
      if (!keyword.value.trim()) return
      searched.value = true
      try {
        const res = await searchApi.search({
          keyword: keyword.value.trim(),
          notebookId: filterNotebookId.value || undefined,
          tagId: filterTagId.value || undefined,
          page: page.value, size
        })
        results.value = res.data.content || []
        total.value = res.data.totalElements || 0
      } catch (e) { /* handled */ }
    }

    onMounted(async () => {
      await Promise.all([
        notebookStore.fetchNotebooks({ size: 50 }),
        tagStore.fetchTags()
      ])
      if (keyword.value) search()
    })

    return { keyword, filterNotebookId, filterTagId, results, total, page, size, searched, search, router, notebookStore, tagStore }
  }
}
</script>
