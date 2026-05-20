<template>
  <div style="flex:1; overflow-y:auto;">
    <el-table :data="noteStore.notes" stripe v-loading="noteStore.loading"
      @row-click="(row) => router.push(`/notes/${row.id}/edit`)"
      style="width:100%; cursor:pointer;" @selection-change="onSelectionChange">
      <el-table-column type="selection" width="40" />
      <el-table-column prop="title" label="标题" min-width="200">
        <template #default="{ row }">
          <span v-html="row.title"></span>
        </template>
      </el-table-column>
      <el-table-column prop="notebookName" label="笔记本" width="120" />
      <el-table-column label="标签" width="180">
        <template #default="{ row }">
          <el-tag v-for="tag in row.tags" :key="tag.id" :color="tag.color"
            size="small" style="color:#fff; margin-right:4px;">{{ tag.name }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" width="160">
        <template #default="{ row }">{{ formatDate(row.updatedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click.stop="deleteNote(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="padding:8px 0; display:flex; gap:8px; align-items:center;">
      <el-button type="primary" size="small" @click="startSummary" :disabled="!selectedIds.length">
        生成阶段总结 ({{ selectedIds.length }})
      </el-button>
      <el-button type="danger" size="small" @click="batchDelete" :disabled="!selectedIds.length">
        批量删除 ({{ selectedIds.length }})
      </el-button>
      <span v-if="!selectedIds.length" style="font-size:12px; color:var(--text-secondary);">
        勾选左侧复选框后可批量操作
      </span>
    </div>

    <el-pagination v-if="noteStore.total > 0" style="margin-top:16px; justify-content:center;"
      :current-page="page" :page-size="size" :total="noteStore.total"
      layout="prev, pager, next" @current-change="onPageChange" />

    <el-dialog v-model="summaryVisible" title="阶段总结" width="700px" :close-on-click-modal="false">
      <div v-if="summarizing" style="text-align:center; padding:20px;">
        <el-progress :percentage="summaryProgress" :stroke-width="6" />
        <p style="margin-top:12px; color:var(--text-secondary);">正在生成阶段总结…</p>
      </div>
      <div v-if="summaryContent" v-html="renderedSummary" class="summary-content"></div>
      <div v-if="summaryError" style="color:red;">{{ summaryError }}</div>
      <template #footer v-if="!summarizing">
        <el-button @click="summaryVisible = false">关闭</el-button>
        <el-button type="primary" @click="saveSummary" :disabled="!summaryContent">存为新笔记</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useNoteStore } from '../stores/note'
import { noteApi } from '../api/notes'
import { marked } from 'marked'

export default {
  name: 'NoteList',
  props: { notebookId: String, tagId: String, sort: { type: String, default: 'updatedAt,desc' } },
  setup(props) {
    const router = useRouter()
    const noteStore = useNoteStore()
    const page = ref(1)
    const size = 20
    const selectedIds = ref([])

    const load = () => {
      noteStore.fetchNotes({
        notebookId: props.notebookId || undefined,
        tagId: props.tagId || undefined,
        page: page.value, size, sort: props.sort
      })
    }

    const onSelectionChange = (rows) => { selectedIds.value = rows.map(r => r.id) }
    const onPageChange = (p) => { page.value = p; load() }

    const deleteNote = async (id) => {
      try {
        await ElMessageBox.confirm('确定要删除这篇笔记吗？', '确认删除', { type: 'warning' })
        await noteStore.remove(id)
        load()
      } catch (e) { /* cancelled */ }
    }

    const batchDelete = async () => {
      try {
        await ElMessageBox.confirm(`确定要删除 ${selectedIds.value.length} 篇笔记吗？`, '确认批量删除', { type: 'warning' })
        await noteStore.batchRemove(selectedIds.value)
        selectedIds.value = []
        load()
      } catch (e) { /* cancelled */ }
    }

    const summaryVisible = ref(false)
    const summarizing = ref(false)
    const summaryContent = ref('')
    const summaryError = ref('')
    const summaryProgress = ref(0)
    const renderedSummary = computed(() => summaryContent.value ? marked.parse(summaryContent.value) : '')

    const startSummary = async () => {
      summaryVisible.value = true
      summarizing.value = true
      summaryContent.value = ''
      summaryError.value = ''
      summaryProgress.value = 10

      try {
        const r = await fetch('/api/v1/notes/summarize', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ noteIds: selectedIds.value })
        })
        if (!r.ok) throw new Error('请求失败: ' + r.statusText)

        const reader = r.body.getReader()
        const decoder = new TextDecoder()
        let buffer = ''

        while (true) {
          const { done, value } = await reader.read()
          if (done) break
          buffer += decoder.decode(value, { stream: true })
          const lines = buffer.split('\n')
          buffer = lines.pop() || ''
          for (const line of lines) {
            if (line.startsWith('event:analysis')) continue
            if (line.startsWith('data:')) {
              try {
                const event = JSON.parse(line.substring(5).trim())
                if (event.phase === 'ERROR') {
                  summaryError.value = event.content
                } else if (event.phase === 'DONE') {
                  summaryProgress.value = 100
                } else {
                  summaryContent.value += event.content || ''
                  summaryProgress.value = 50
                }
              } catch (e) { /* skip */ }
            }
          }
        }
      } catch (e) {
        summaryError.value = e.message || '总结失败'
      } finally {
        summarizing.value = false
      }
    }

    const saveSummary = async () => {
      try {
        const title = '阶段总结 - ' + new Date().toLocaleDateString('zh-CN')
        await noteApi.create({ title, content: summaryContent.value, notebookId: props.notebookId || undefined })
        ElMessage.success('已保存为笔记')
        summaryVisible.value = false
        summaryContent.value = ''
        load()
      } catch (e) {
        ElMessage.error('保存失败')
      }
    }

    const formatDate = (d) => d ? new Date(d).toLocaleString('zh-CN') : ''

    watch(() => [props.notebookId, props.tagId, props.sort], () => { page.value = 1; load() })
    onMounted(load)

    return { noteStore, router, page, size, selectedIds, load, onSelectionChange, onPageChange,
      deleteNote, batchDelete, formatDate,
      summaryVisible, summarizing, summaryContent, summaryError, summaryProgress, renderedSummary,
      startSummary, saveSummary }
  }
}
</script>

<style scoped>
.summary-content {
  max-height: 500px;
  overflow-y: auto;
  line-height: 1.7;
}
.summary-content :deep(h2) { margin-top: 20px; border-bottom: 1px solid var(--border-color); padding-bottom: 6px; }
.summary-content :deep(h3) { margin-top: 16px; }
.summary-content :deep(code) { background: var(--bg-secondary); padding: 2px 6px; border-radius: 4px; }
.summary-content :deep(pre) { background: var(--bg-secondary); padding: 12px; border-radius: 8px; overflow-x: auto; }
</style>
