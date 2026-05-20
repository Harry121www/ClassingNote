<template>
  <div class="knowledge-base">
    <h3>知识库</h3>
    <div v-if="!searchProject" class="knowledge-base__empty">
      <p>输入项目名搜索知识库条目</p>
    </div>
    <div v-else-if="entries.length === 0 && !loading" class="knowledge-base__empty">
      <p>暂无知识库条目</p>
      <p class="knowledge-base__hint">通过「项目分析」生成的知识点可以保存到此处</p>
    </div>
    <div v-else>
      <el-input v-model="searchProject" placeholder="按项目名搜索…" size="small" clearable
        style="width:300px; margin-bottom:16px;" @input="loadEntries" />
      <el-card v-for="entry in entries" :key="entry.id" class="knowledge-card" shadow="hover">
        <template #header>
          <div class="knowledge-card__header">
            <span>{{ entry.title }}</span>
            <el-tag size="small" :type="categoryType(entry.category)">
              {{ entry.category }}
            </el-tag>
          </div>
        </template>
        <div class="knowledge-card__meta">
          <span>项目: {{ entry.projectName }}</span>
          <span v-if="entry.packageName">包: {{ entry.packageName }}</span>
        </div>
        <div v-html="renderedPreview(entry.content)" class="knowledge-card__preview"></div>
        <div class="knowledge-card__actions">
          <el-button size="small" text @click="viewDetail(entry)">查看详情</el-button>
          <el-button size="small" text type="danger" @click="deleteEntry(entry.id)">删除</el-button>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="detailVisible" :title="detailEntry?.title" width="700px">
      <div v-if="detailEntry" v-html="renderedFull(detailEntry.content)"></div>
    </el-dialog>
  </div>
</template>

<script>
import { ref } from 'vue'
import { knowledgeApi } from '../api/knowledge'
import { ElMessage, ElMessageBox } from 'element-plus'
import { marked } from 'marked'

export default {
  name: 'KnowledgeBase',
  setup() {
    const entries = ref([])
    const searchProject = ref('')
    const loading = ref(false)
    const detailVisible = ref(false)
    const detailEntry = ref(null)

    async function loadEntries() {
      if (!searchProject.value) {
        entries.value = []
        return
      }
      loading.value = true
      try {
        const res = await knowledgeApi.listByProject(searchProject.value)
        entries.value = res.data || []
      } catch (e) {
        ElMessage.error('加载失败')
      } finally {
        loading.value = false
      }
    }

    function categoryType(cat) {
      return cat === 'OVERVIEW' ? 'primary' : cat === 'PACKAGE' ? 'success' : 'info'
    }

    function renderedPreview(content) {
      if (!content) return ''
      return marked.parse(content.substring(0, 300) + '...')
    }

    function renderedFull(content) {
      return content ? marked.parse(content) : ''
    }

    function viewDetail(entry) {
      detailEntry.value = entry
      detailVisible.value = true
    }

    async function deleteEntry(id) {
      try {
        await ElMessageBox.confirm('确认删除该知识库条目？', '确认')
        await knowledgeApi.delete(id)
        entries.value = entries.value.filter(e => e.id !== id)
        ElMessage.success('已删除')
      } catch (e) {
        // cancelled
      }
    }

    return { entries, searchProject, loading, detailVisible, detailEntry,
      loadEntries, categoryType, renderedPreview, renderedFull, viewDetail, deleteEntry }
  }
}
</script>

<style scoped>
.knowledge-base {
  padding: 24px;
  max-width: 900px;
  margin: 0 auto;
  height: 100%;
  overflow-y: auto;
}
.knowledge-base h3 { margin: 0 0 20px; font-size: 18px; }
.knowledge-base__empty { text-align: center; padding: 60px 0; color: var(--text-secondary); }
.knowledge-base__hint { font-size: 13px; margin-top: 8px; }
.knowledge-card { margin-bottom: 12px; }
.knowledge-card__header { display: flex; justify-content: space-between; align-items: center; }
.knowledge-card__meta {
  display: flex; gap: 16px; font-size: 12px; color: var(--text-secondary); margin-bottom: 8px;
}
.knowledge-card__preview { font-size: 13px; line-height: 1.6; max-height: 80px; overflow: hidden; }
.knowledge-card__actions { margin-top: 10px; display: flex; gap: 8px; }
</style>
