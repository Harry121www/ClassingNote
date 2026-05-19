<template>
  <el-dialog :model-value="visible" @update:model-value="$emit('update:visible', $event)"
    title="新建笔记本" width="450px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="名称" required>
        <el-input v-model="form.name" placeholder="笔记本名称" maxlength="100" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" placeholder="可选描述" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" @click="submit" :disabled="!form.name.trim()">创建</el-button>
    </template>
  </el-dialog>
</template>

<script>
import { reactive } from 'vue'
import { useNotebookStore } from '../stores/notebook'

export default {
  name: 'NotebookDialog',
  props: { visible: Boolean },
  emits: ['update:visible', 'saved'],
  setup(props, { emit }) {
    const notebookStore = useNotebookStore()
    const form = reactive({ name: '', description: '' })

    const submit = async () => {
      await notebookStore.create({ name: form.name.trim(), description: form.description.trim() })
      form.name = ''; form.description = ''
      emit('update:visible', false)
      emit('saved')
    }

    return { form, submit }
  }
}
</script>
