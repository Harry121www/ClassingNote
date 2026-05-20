<template>
  <div
    class="drop-zone"
    :class="{ 'drop-zone--active': dragging }"
    @dragover.prevent="dragging = true"
    @dragleave.prevent="dragging = false"
    @drop.prevent="handleDrop"
  >
    <el-icon :size="48"><UploadFilled /></el-icon>
    <p>拖拽项目文件夹到此处</p>
    <p class="drop-zone__hint">支持 Java、Python、JavaScript 等项目源码文件夹</p>
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
    }

    return { dragging, handleDrop }
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
