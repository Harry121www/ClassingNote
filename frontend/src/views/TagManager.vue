<template>
  <div style="padding:20px;">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
      <h3>标签管理</h3>
      <el-button type="primary" @click="dialogVisible = true; editingTag = null;">
        <el-icon><Plus /></el-icon> 新建标签
      </el-button>
    </div>

    <div v-if="tagStore.tags.length === 0" style="text-align:center; color:var(--text-secondary); padding:60px;">
      暂无标签，点击上方按钮创建
    </div>

    <el-table :data="tagStore.tags" v-loading="tagStore.loading" style="width:100%;">
      <el-table-column label="颜色" width="80">
        <template #default="{ row }">
          <div :style="{ width:'24px', height:'24px', background:row.color, borderRadius:'4px' }"></div>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="noteCount" label="关联笔记数" width="120" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="editTag(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteTag(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <TagDialog v-model:visible="dialogVisible" :editing="editingTag" @saved="tagStore.fetchTags()" />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { useTagStore } from '../stores/tag'
import TagDialog from '../components/TagDialog.vue'

export default {
  name: 'TagManager',
  components: { TagDialog },
  setup() {
    const tagStore = useTagStore()
    const dialogVisible = ref(false)
    const editingTag = ref(null)

    const editTag = (tag) => { editingTag.value = tag; dialogVisible.value = true }
    const deleteTag = async (id) => {
      try {
        await ElMessageBox.confirm('确定要删除这个标签吗？将解除与所有笔记的关联。', '确认删除', { type: 'warning' })
        await tagStore.remove(id)
      } catch (e) { /* cancelled */ }
    }

    onMounted(() => tagStore.fetchTags())

    return { tagStore, dialogVisible, editingTag, editTag, deleteTag }
  }
}
</script>
