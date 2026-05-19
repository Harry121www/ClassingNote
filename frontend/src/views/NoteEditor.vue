<template>
  <div style="height:100%; display:flex; flex-direction:column;">
    <div style="padding:12px 20px; border-bottom:1px solid #e4e7ed; display:flex; align-items:center; gap:12px;">
      <el-input v-model="title" placeholder="笔记标题" style="width:300px;" maxlength="200" />
      <el-select v-model="notebookId" placeholder="选择笔记本" style="width:180px;">
        <el-option v-for="nb in notebookStore.notebooks" :key="nb.id"
          :label="nb.name" :value="nb.id" />
      </el-select>
      <el-select v-model="selectedTagIds" placeholder="选择标签" style="width:200px;" multiple>
        <el-option v-for="tag in tagStore.tags" :key="tag.id" :label="tag.name" :value="tag.id" />
      </el-select>
      <el-button @click="uploadRef.click()" size="small">
        <el-icon><Upload /></el-icon> 插入附件
      </el-button>
      <input ref="uploadRef" type="file" hidden @change="onFileUpload" />
      <div style="flex:1;" />
      <el-button @click="save" type="primary" :disabled="!canSave">保存</el-button>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <div style="flex:1; display:flex; overflow:hidden;">
      <MarkdownEditor v-model="content" :font-size="prefStore.editorFontSize"
        style="flex:1; border-right:1px solid #e4e7ed;" />
      <MarkdownPreview :content="content" style="flex:1;" />
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useNoteStore } from '../stores/note'
import { useNotebookStore } from '../stores/notebook'
import { useTagStore } from '../stores/tag'
import { usePreferenceStore } from '../stores/preference'
import { useDraftStore } from '../stores/draft'
import { attachmentApi } from '../api/attachments'
import MarkdownEditor from '../components/MarkdownEditor.vue'
import MarkdownPreview from '../components/MarkdownPreview.vue'

export default {
  name: 'NoteEditor',
  components: { MarkdownEditor, MarkdownPreview },
  props: { id: String },
  setup(props) {
    const route = useRoute()
    const router = useRouter()
    const noteStore = useNoteStore()
    const notebookStore = useNotebookStore()
    const tagStore = useTagStore()
    const prefStore = usePreferenceStore()
    const draftStore = useDraftStore()

    const noteId = ref(props.id || null)
    const title = ref('')
    const content = ref('')
    const notebookId = ref('')
    const selectedTagIds = ref([])
    const uploadRef = ref(null)
    let autoSaveTimer = null

    const canSave = computed(() => title.value.trim() && notebookId.value)

    const save = async () => {
      const data = {
        title: title.value.trim(),
        content: content.value,
        notebookId: notebookId.value,
        tagIds: selectedTagIds.value
      }
      if (noteId.value) {
        await noteStore.update(noteId.value, data)
      } else {
        const result = await noteStore.create(data)
        noteId.value = result.id
        router.replace(`/notes/${result.id}/edit`)
      }
      ElMessage.success('保存成功')
      draftStore.clear()
    }

    const onFileUpload = async (e) => {
      const file = e.target.files[0]
      if (!file || !noteId.value) {
        if (!noteId.value) ElMessage.warning('请先保存笔记后再上传附件')
        return
      }
      const fd = new FormData()
      fd.append('file', file)
      fd.append('noteId', noteId.value)
      try {
        const res = await attachmentApi.upload(fd)
        content.value += '\n' + res.data.markdownRef
      } catch (e) { /* handled by interceptor */ }
      uploadRef.value.value = ''
    }

    const startAutoSave = () => {
      autoSaveTimer = setInterval(() => {
        if (title.value.trim()) {
          draftStore.save({
            noteId: noteId.value,
            title: title.value,
            content: content.value,
            notebookId: notebookId.value
          })
        }
      }, prefStore.autoSaveInterval * 1000)
    }

    onMounted(async () => {
      await Promise.all([
        notebookStore.fetchNotebooks({ size: 50 }),
        tagStore.fetchTags()
      ])

      if (noteId.value) {
        const note = await noteStore.fetchNote(noteId.value)
        title.value = note.title
        content.value = note.content || ''
        notebookId.value = note.notebookId
        selectedTagIds.value = note.tags.map(t => t.id)
      } else {
        const draft = await draftStore.fetchLatest()
        if (draft) {
          title.value = draft.title || ''
          content.value = draft.content || ''
          notebookId.value = draft.notebookId || ''
        }
      }
      startAutoSave()
    })

    onUnmounted(() => { clearInterval(autoSaveTimer) })

    return {
      title, content, notebookId, selectedTagIds, uploadRef, canSave, save, onFileUpload,
      router, notebookStore, tagStore, prefStore, noteStore
    }
  }
}
</script>
