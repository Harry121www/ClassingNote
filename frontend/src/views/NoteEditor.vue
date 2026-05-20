<template>
  <div style="height:100%; display:flex; flex-direction:column;">

    <!-- Preview mode toolbar -->
    <div v-if="viewMode === 'preview'" style="padding:8px 20px; border-bottom:1px solid var(--border-color); display:flex; align-items:center; gap:8px;">
      <h2 style="margin:0; font-size:16px; flex:1;">{{ title || '无标题' }}</h2>
      <span v-if="notebookId" style="font-size:12px; color:var(--text-secondary);">{{ notebookName }}</span>
      <span v-for="tag in selectedTagNames" :key="tag" style="font-size:12px; color:var(--text-secondary);">#{{ tag }}</span>
      <el-button @click="viewMode = 'edit'">编辑</el-button>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <!-- Edit mode toolbar -->
    <div v-if="viewMode === 'edit'" style="padding:8px 20px; border-bottom:1px solid var(--border-color); display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
      <el-input v-model="title" placeholder="笔记标题" style="width:240px;" size="small" maxlength="200" />
      <el-select v-model="notebookId" placeholder="笔记本" style="width:150px;" size="small">
        <el-option v-for="nb in notebookStore.notebooks" :key="nb.id" :label="nb.name" :value="nb.id" />
      </el-select>
      <el-select v-model="selectedTagIds" placeholder="标签" style="width:160px;" size="small" multiple>
        <el-option v-for="tag in tagStore.tags" :key="tag.id" :label="tag.name" :value="tag.id" />
      </el-select>
      <el-color-picker v-model="textColor" size="small" />
      <el-button size="small" @click="applyColor" :disabled="!textColor">文字颜色</el-button>
      <el-button @click="uploadRef.click()" size="small">
        <el-icon><Upload /></el-icon> 附件
      </el-button>
      <input ref="uploadRef" type="file" hidden @change="onFileUpload" />
      <div style="flex:1;" />
      <el-button @click="viewMode = 'preview'" size="small">预览</el-button>
      <el-button @click="save" type="primary" size="small" :disabled="!canSave">保存</el-button>
      <el-button @click="router.back()" size="small">返回</el-button>
    </div>

    <!-- Preview content -->
    <div v-if="viewMode === 'preview'" style="flex:1; overflow-y:auto;">
      <MarkdownPreview :content="content" style="max-width:900px; margin:0 auto;" />
    </div>

    <!-- Edit content -->
    <div v-if="viewMode === 'edit'" style="flex:1; display:flex; overflow:hidden;">
      <MarkdownEditor ref="editorRef" v-model="content" :font-size="prefStore.editorFontSize"
        style="flex:1; border-right:1px solid var(--border-color);" />
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
    const viewMode = ref(props.id ? 'preview' : 'edit')
    const textColor = ref('#409EFF')
    let autoSaveTimer = null

    const canSave = computed(() => title.value.trim() && notebookId.value)

    const notebookName = computed(() => {
      const nb = notebookStore.notebooks.find(n => n.id === notebookId.value)
      return nb ? nb.name : ''
    })

    const selectedTagNames = computed(() => {
      return tagStore.tags
        .filter(t => selectedTagIds.value.includes(t.id))
        .map(t => t.name)
    })

    const editorRef = ref(null)

    const applyColor = () => {
      if (!textColor.value || !editorRef.value) return
      const sel = editorRef.value.getSelection()
      if (sel && sel.text) {
        editorRef.value.wrapSelection(
          `<span style="color:${textColor.value};">`,
          '</span>'
        )
      } else {
        editorRef.value.insertAtCursor(
          `<span style="color:${textColor.value};">文字</span>`
        )
      }
    }

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
      title, content, notebookId, selectedTagIds, uploadRef, viewMode, textColor,
      canSave, notebookName, selectedTagNames, editorRef, applyColor,
      save, onFileUpload,
      router, notebookStore, tagStore, prefStore, noteStore
    }
  }
}
</script>
