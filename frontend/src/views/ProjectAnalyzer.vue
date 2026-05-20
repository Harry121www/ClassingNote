<template>
  <div class="project-analyzer">
    <div class="project-analyzer__header">
      <h3>项目分析</h3>
      <el-button v-if="store.phase === 'done'" type="primary" @click="showSaveDialog = true">
        保存结果
      </el-button>
    </div>

    <FolderDropZone
      v-if="store.phase === 'idle'"
      @filesLoaded="onFilesLoaded"
    />

    <FileTreeSelector
      v-if="store.phase === 'idle' && store.fileTree.length > 0"
      :files="store.fileTree"
      @toggleFile="store.toggleFile"
      @selectAll="store.selectAll"
      @deselectAll="store.deselectAll"
    />

    <div v-if="store.phase === 'idle' && store.fileTree.length > 0" class="project-analyzer__actions">
      <el-button type="primary" :disabled="store.getSelectedFiles().length === 0"
        @click="startAnalysis">
        开始分析 ({{ store.getSelectedFiles().length }} 个文件)
      </el-button>
      <el-button @click="store.reset()">清除</el-button>
    </div>

    <AnalysisProgress
      :phase="store.phase"
      :currentStage="store.currentStage"
      :currentTarget="store.currentTarget"
    />

    <div v-if="store.phase === 'error'" class="project-analyzer__error">
      <el-alert :title="store.error" type="error" show-icon :closable="false" />
      <el-button style="margin-top:12px" @click="store.reset()">重试</el-button>
    </div>

    <AnalysisResult :content="store.streamContent" />

    <SaveResultDialog
      v-model="showSaveDialog"
      :content="store.streamContent"
      :projectName="store.projectName"
      @save="handleSave"
    />
  </div>
</template>

<script>
import { ref } from 'vue'
import { useAnalysisStore } from '../stores/analysis'
import { streamAnalysis } from '../api/analysis'
import { knowledgeApi } from '../api/knowledge'
import { noteApi } from '../api/notes'
import FolderDropZone from '../components/analysis/FolderDropZone.vue'
import FileTreeSelector from '../components/analysis/FileTreeSelector.vue'
import AnalysisProgress from '../components/analysis/AnalysisProgress.vue'
import AnalysisResult from '../components/analysis/AnalysisResult.vue'
import SaveResultDialog from '../components/analysis/SaveResultDialog.vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'ProjectAnalyzer',
  components: { FolderDropZone, FileTreeSelector, AnalysisProgress, AnalysisResult, SaveResultDialog },
  setup() {
    const store = useAnalysisStore()
    const showSaveDialog = ref(false)

    function onFilesLoaded({ files, projectName }) {
      store.setFileTree(files)
      store.projectName = projectName
    }

    async function startAnalysis() {
      const selectedFiles = store.getSelectedFiles()
      if (selectedFiles.length === 0) return
      store.resetAnalysis()

      const { response, controller } = streamAnalysis(selectedFiles, store.projectName)

      try {
        const r = await response
        if (!r.ok) throw new Error('Analysis request failed: ' + r.statusText)
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
                store.handleEvent(event)
              } catch (e) {
                // skip parse errors
              }
            }
          }
        }
      } catch (e) {
        if (e.name !== 'AbortError') {
          store.setError(e.message || 'Analysis failed')
        }
      }
    }

    async function handleSave({ saveType, title, notebookId, projectName, content }) {
      try {
        if (saveType === 'note' || saveType === 'both') {
          if (!notebookId) {
            ElMessage.warning('请选择一个笔记本')
            return
          }
          await noteApi.create({ title, content, notebookId })
          ElMessage.success('已保存为笔记')
        }
        if (saveType === 'knowledge' || saveType === 'both') {
          await knowledgeApi.create({
            title,
            content,
            projectName,
            category: 'OVERVIEW'
          })
          ElMessage.success('已保存到知识库')
        }
        showSaveDialog.value = false
      } catch (e) {
        ElMessage.error('保存失败')
      }
    }

    store.reset()
    return { store, showSaveDialog, onFilesLoaded, startAnalysis, handleSave }
  }
}
</script>

<style scoped>
.project-analyzer {
  padding: 24px;
  max-width: 960px;
  margin: 0 auto;
  height: 100%;
  overflow-y: auto;
}
.project-analyzer__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.project-analyzer__header h3 {
  margin: 0;
  font-size: 18px;
}
.project-analyzer__actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}
.project-analyzer__error {
  margin-top: 20px;
}
</style>
