<template>
  <el-dialog v-model="visible" title="保存分析结果" width="400px" @close="handleClose">
    <el-form :model="form" label-width="60px">
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="输入标题" />
      </el-form-item>
      <el-form-item label="笔记本">
        <el-select v-model="form.notebookId" placeholder="选择笔记本" style="width:100%">
          <el-option v-for="nb in notebooks" :key="nb.id" :label="nb.name" :value="nb.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleSave" :loading="saving">存为笔记</el-button>
    </template>
  </el-dialog>
</template>

<script>
import { ref, watch } from 'vue'
import { notebookApi } from '../../api/notebooks'
import { useNotebookStore } from '../../stores/notebook'

export default {
  name: 'SaveResultDialog',
  props: {
    modelValue: Boolean,
    content: String,
    projectName: String
  },
  emits: ['update:modelValue', 'save'],
  setup(props, { emit }) {
    const visible = ref(false)
    const saving = ref(false)
    const notebooks = ref([])
    const form = ref({
      title: '',
      notebookId: ''
    })

    watch(() => props.modelValue, (val) => {
      visible.value = val
      if (val) {
        form.value.title = (props.projectName || 'Project') + ' 分析报告'
        loadNotebooks()
      }
    })

    async function loadNotebooks() {
      const res = await notebookApi.list({ size: 50 })
      notebooks.value = res.data.content || []
      const nbStore = useNotebookStore()
      if (nbStore.currentNotebookId) {
        form.value.notebookId = nbStore.currentNotebookId
      }
    }

    function handleClose() {
      emit('update:modelValue', false)
    }

    function handleSave() {
      emit('save', { ...form.value, content: props.content })
    }

    return { visible, saving, notebooks, form, handleClose, handleSave }
  }
}
</script>
