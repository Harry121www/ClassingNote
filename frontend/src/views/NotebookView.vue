<template>
  <div style="height:100%; display:flex; flex-direction:column; padding:20px;">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
      <div>
        <el-button type="primary" @click="showNotebookDialog = true">
          <el-icon><Plus /></el-icon> 新建笔记本
        </el-button>
        <el-button @click="router.push('/notes/new')" type="success">
          <el-icon><Edit /></el-icon> 新建笔记
        </el-button>
      </div>
      <el-radio-group v-model="sort" size="small" @change="loadNotes">
        <el-radio-button value="updatedAt,desc">最近更新</el-radio-button>
        <el-radio-button value="createdAt,desc">最近创建</el-radio-button>
        <el-radio-button value="title,asc">标题排序</el-radio-button>
      </el-radio-group>
    </div>

    <NoteList :notebook-id="notebookId" :tag-id="tagId" :sort="sort" ref="noteListRef" />

    <NotebookDialog v-model:visible="showNotebookDialog" @saved="onNotebookSaved" />
  </div>
</template>

<script>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NoteList from '../components/NoteList.vue'
import NotebookDialog from '../components/NotebookDialog.vue'

export default {
  name: 'NotebookView',
  components: { NoteList, NotebookDialog },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const showNotebookDialog = ref(false)
    const sort = ref('updatedAt,desc')
    const notebookId = ref(route.query.notebookId || '')
    const tagId = ref(route.query.tagId || '')
    const noteListRef = ref(null)

    watch(() => route.query, (q) => {
      notebookId.value = q.notebookId || ''
      tagId.value = q.tagId || ''
    })

    const loadNotes = () => noteListRef.value?.load()
    const onNotebookSaved = () => noteListRef.value?.load()

    return { showNotebookDialog, sort, notebookId, tagId, noteListRef, loadNotes, onNotebookSaved, router }
  }
}
</script>
