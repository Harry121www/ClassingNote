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

    <div v-if="selectedIds.length" style="padding:8px 0;">
      <el-button type="danger" size="small" @click="batchDelete">
        批量删除 ({{ selectedIds.length }})
      </el-button>
    </div>

    <el-pagination v-if="noteStore.total > 0" style="margin-top:16px; justify-content:center;"
      :current-page="page" :page-size="size" :total="noteStore.total"
      layout="prev, pager, next" @current-change="onPageChange" />
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useNoteStore } from '../stores/note'

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

    const formatDate = (d) => d ? new Date(d).toLocaleString('zh-CN') : ''

    watch(() => [props.notebookId, props.tagId, props.sort], () => { page.value = 1; load() })
    onMounted(load)

    return { noteStore, router, page, size, selectedIds, load, onSelectionChange, onPageChange, deleteNote, batchDelete, formatDate }
  }
}
</script>
