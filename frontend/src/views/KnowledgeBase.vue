<template>
  <div class="knowledge-base">
    <div class="knowledge-base__header">
      <h3>知识库</h3>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon> 新建条目
      </el-button>
    </div>

    <el-input v-model="searchProject" placeholder="按项目名搜索…" size="small" clearable
      style="width:300px; margin-bottom:16px;" @input="loadEntries" />

    <div v-if="!searchProject" class="knowledge-base__empty">
      <p>输入项目名搜索知识库条目</p>
    </div>
    <div v-else-if="entries.length === 0 && !loading" class="knowledge-base__empty">
      <p>暂无知识库条目</p>
    </div>
    <div v-else>
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
          <el-button size="small" text @click="openEdit(entry)">编辑</el-button>
          <el-button size="small" text type="danger" @click="deleteEntry(entry.id)">删除</el-button>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="detailVisible" :title="detailEntry?.title" width="700px">
      <div v-if="detailEntry" v-html="renderedFull(detailEntry.content)"></div>
    </el-dialog>

    <el-dialog v-model="editVisible" :title="editingEntry ? '编辑条目' : '新建条目'" width="600px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="editForm.title" placeholder="条目标题" />
        </el-form-item>
        <el-form-item label="项目名" required>
          <el-input v-model="editForm.projectName" placeholder="所属项目" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editForm.category" style="width:100%">
            <el-option label="项目总览" value="OVERVIEW" />
            <el-option label="包级汇总" value="PACKAGE" />
            <el-option label="逐文件" value="PER_FILE" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件路径">
          <el-input v-model="editForm.filePath" placeholder="如: src/main/UserService.java" />
        </el-form-item>
        <el-form-item label="包名">
          <el-input v-model="editForm.packageName" placeholder="如: com.example.service" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="editForm.content" type="textarea" :rows="12" placeholder="Markdown 内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEntry" :loading="saving"
          :disabled="!editForm.title.trim() || !editForm.projectName.trim() || !editForm.content.trim()">
          保存
        </el-button>
      </template>
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
    const saving = ref(false)
    const detailVisible = ref(false)
    const detailEntry = ref(null)
    const editVisible = ref(false)
    const editingEntry = ref(null)
    const editForm = ref({
      title: '', projectName: '', category: 'OVERVIEW',
      filePath: '', packageName: '', content: ''
    })

    async function loadEntries() {
      if (!searchProject.value) { entries.value = []; return }
      loading.value = true
      try {
        const res = await knowledgeApi.listByProject(searchProject.value)
        entries.value = res.data || []
      } catch (e) {
        ElMessage.error('加载失败')
      } finally { loading.value = false }
    }

    function categoryType(cat) {
      return cat === 'OVERVIEW' ? 'primary' : cat === 'PACKAGE' ? 'success' : 'info'
    }
    function renderedPreview(c) { return c ? marked.parse(c.substring(0, 300) + '...') : '' }
    function renderedFull(c) { return c ? marked.parse(c) : '' }

    function viewDetail(entry) { detailEntry.value = entry; detailVisible.value = true }

    function openCreate() {
      editingEntry.value = null
      editForm.value = { title: '', projectName: searchProject.value || '', category: 'OVERVIEW',
        filePath: '', packageName: '', content: '' }
      editVisible.value = true
    }

    function openEdit(entry) {
      editingEntry.value = entry
      editForm.value = {
        title: entry.title, projectName: entry.projectName, category: entry.category,
        filePath: entry.filePath || '', packageName: entry.packageName || '', content: entry.content
      }
      editVisible.value = true
    }

    async function saveEntry() {
      saving.value = true
      try {
        if (editingEntry.value) {
          await knowledgeApi.update(editingEntry.value.id, {
            title: editForm.value.title.trim(),
            projectName: editForm.value.projectName.trim(),
            category: editForm.value.category,
            filePath: editForm.value.filePath || undefined,
            packageName: editForm.value.packageName || undefined,
            content: editForm.value.content.trim()
          })
        } else {
          await knowledgeApi.create({
            title: editForm.value.title.trim(),
            projectName: editForm.value.projectName.trim(),
            category: editForm.value.category,
            filePath: editForm.value.filePath || undefined,
            packageName: editForm.value.packageName || undefined,
            content: editForm.value.content.trim()
          })
        }
        ElMessage.success(editingEntry.value ? '已更新' : '已创建')
        editVisible.value = false
        await loadEntries()
      } catch (e) {
        ElMessage.error('保存失败')
      } finally { saving.value = false }
    }

    async function deleteEntry(id) {
      try {
        await ElMessageBox.confirm('确认删除该知识库条目？', '确认')
        await knowledgeApi.delete(id)
        entries.value = entries.value.filter(e => e.id !== id)
        ElMessage.success('已删除')
      } catch (e) { /* cancelled */ }
    }

    return { entries, searchProject, loading, saving, detailVisible, detailEntry,
      editVisible, editingEntry, editForm,
      loadEntries, categoryType, renderedPreview, renderedFull,
      viewDetail, openCreate, openEdit, saveEntry, deleteEntry }
  }
}
</script>

<style scoped>
.knowledge-base {
  padding: 24px; max-width: 900px; margin: 0 auto; height: 100%; overflow-y: auto;
}
.knowledge-base__header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
}
.knowledge-base__header h3 { margin: 0; font-size: 18px; }
.knowledge-base__empty { text-align: center; padding: 60px 0; color: var(--text-secondary); }
.knowledge-card { margin-bottom: 12px; }
.knowledge-card__header { display: flex; justify-content: space-between; align-items: center; }
.knowledge-card__meta {
  display: flex; gap: 16px; font-size: 12px; color: var(--text-secondary); margin-bottom: 8px;
}
.knowledge-card__preview { font-size: 13px; line-height: 1.6; max-height: 80px; overflow: hidden; }
.knowledge-card__actions { margin-top: 10px; display: flex; gap: 8px; }
</style>
