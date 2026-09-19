<template>
  <el-dialog :model-value="visible" @update:model-value="$emit('update:visible', $event)"
    :title="editing ? '编辑标签' : '新建标签'" width="400px">
    <el-form :model="form" label-width="60px">
      <el-form-item label="名称" required>
        <el-input v-model="form.name" maxlength="30" placeholder="标签名称" />
      </el-form-item>
      <el-form-item label="颜色">
        <el-color-picker v-model="form.color" />
        <span style="margin-left:8px; color:var(--text-secondary);">{{ form.color }}</span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" @click="submit" :disabled="!form.name.trim()">
        {{ editing ? '保存' : '创建' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script>
import { reactive, watch } from 'vue'
import { useTagStore } from '../stores/tag'

export default {
  name: 'TagDialog',
  props: { visible: Boolean, editing: Object },
  emits: ['update:visible', 'saved'],
  setup(props, { emit }) {
    const tagStore = useTagStore()
    const form = reactive({ name: '', color: '#409EFF' })

    watch(() => props.editing, (tag) => {
      if (tag) { form.name = tag.name; form.color = tag.color }
      else { form.name = ''; form.color = '#409EFF' }
    })

    const submit = async () => {
      if (props.editing) {
        await tagStore.update(props.editing.id, { name: form.name.trim(), color: form.color })
      } else {
        await tagStore.create({ name: form.name.trim(), color: form.color })
      }
      emit('update:visible', false)
      emit('saved')
    }

    return { form, submit }
  }
}
</script>
