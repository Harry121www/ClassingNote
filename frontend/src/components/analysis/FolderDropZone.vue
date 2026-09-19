<template>
  <div
    class="drop-zone"
    :class="{ 'drop-zone--active': dragging }"
    @dragover.prevent="dragging = true"
    @dragleave.prevent="dragging = false"
    @drop.prevent="handleDrop"
    @click="openFolderPicker"
  >
    <el-icon :size="48"><UploadFilled /></el-icon>
    <p>拖拽项目文件夹到此处，或点击选择</p>
    <p class="drop-zone__hint">支持 Java、Python、JavaScript 等项目源码文件夹</p>
    <input
      ref="folderInput"
      type="file"
      webkitdirectory
      hidden
      @change="handleFolderPick"
    />
  </div>
</template>

<script>
import { ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'

export default {
  name: 'FolderDropZone',
  components: { UploadFilled },
  emits: ['filesLoaded'],
  setup(props, { emit }) {
    const dragging = ref(false)
    const folderInput = ref(null)

    async function openFolderPicker() {
      try {
        const res = await fetch('/api/v1/analysis/pick-folder', { method: 'POST' })
        const result = await res.json()
        if (!result || result.length === 0) return
        const files = result.map(f => ({
          path: f.path,
          content: f.content,
          selected: true
        }))
        const projectName = files[0].path.split('/')[0] || 'Unknown Project'
        emit('filesLoaded', { files, projectName })
      } catch (e) {
        // fallback to browser file input
        folderInput.value.value = ''
        folderInput.value.click()
      }
    }

    async function handleFolderPick(e) {
      const fileList = [...e.target.files]
      if (fileList.length === 0) return
      const readAll = fileList.map((f) => {
        return new Promise((resolve) => {
          const reader = new FileReader()
          reader.onload = () => resolve({
            path: f.webkitRelativePath || f.name,
            content: reader.result,
            selected: true
          })
          reader.onerror = () => resolve({
            path: f.webkitRelativePath || f.name,
            content: null,
            selected: true
          })
          reader.readAsText(f)
        })
      })
      const files = await Promise.all(readAll)
      const projectName = files[0].path.split('/')[0] || 'Unknown Project'
      emit('filesLoaded', { files, projectName })
    }

    async function readEntry(entry) {
      if (entry.isFile) {
        return new Promise((resolve) => {
          entry.file((file) => {
            const reader = new FileReader()
            reader.onload = () => resolve({
              path: file.webkitRelativePath || entry.fullPath.substring(1),
              content: reader.result,
              selected: true
            })
            reader.onerror = () => resolve(null)
            reader.readAsText(file)
          })
        })
      }
      if (entry.isDirectory) {
        const reader = entry.createReader()
        const entries = await new Promise((resolve) => {
          reader.readEntries(resolve)
        })
        const results = await Promise.all(
          entries.map(e => readEntry(e))
        )
        return results.flat().filter(Boolean)
      }
      return []
    }

    async function handleDrop(e) {
      dragging.value = false
      const items = [...e.dataTransfer.items]
      if (items.length === 0) return

      // Check if webkitGetAsEntry is available
      if (items[0].webkitGetAsEntry) {
        const results = []
        for (const item of items) {
          const entry = item.webkitGetAsEntry()
          if (entry) {
            const files = await readEntry(entry)
            results.push(...files)
          }
        }
        if (results.length > 0) {
          const projectName = results[0].path.split('/')[0] || 'Unknown Project'
          emit('filesLoaded', { files: results, projectName })
        }
        return
      }

      // Fallback: use files from dataTransfer
      const fileList = [...e.dataTransfer.files]
      if (fileList.length > 0) {
        const files = fileList.map(f => ({
          path: f.webkitRelativePath || f.name,
          content: null,
          selected: true
        }))
        const projectName = files[0].path.split('/')[0] || 'Unknown Project'
        emit('filesLoaded', { files, projectName })
      }
    }

    return { dragging, folderInput, handleDrop, openFolderPicker, handleFolderPick }
  }
}
</script>

<style scoped>
.drop-zone {
  border: 2px dashed var(--border-color);
  border-radius: 12px;
  padding: 60px 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  color: var(--text-secondary);
}
.drop-zone--active {
  border-color: var(--brand-color);
  background: rgba(var(--brand-color-rgb), 0.05);
  color: var(--brand-color);
}
.drop-zone__hint {
  font-size: 12px;
  margin-top: 8px;
}
</style>
