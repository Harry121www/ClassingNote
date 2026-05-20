<template>
  <el-card class="data-settings" style="margin-top:16px;">
    <template #header><span>数据存储位置</span></template>
    <el-form :model="form" label-width="120px">
      <el-form-item label="数据目录">
        <el-input v-model="form.dataPath" placeholder="默认 ~/.classnote" />
        <div style="font-size:12px; color:var(--text-secondary); margin-top:4px;">
          数据库文件存储位置，修改后需重启生效。留空使用默认。
        </div>
      </el-form-item>
      <el-form-item label="附件目录">
        <el-input v-model="form.attachmentPath" placeholder="默认 ~/classnote-files" />
        <div style="font-size:12px; color:var(--text-secondary); margin-top:4px;">
          上传的图片/文件存储位置。留空使用默认。
        </div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="save" :loading="saving">保存</el-button>
        <span v-if="saved" style="margin-left:12px; color:#67c23a; font-size:13px;">已保存</span>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script>
import { ref, onMounted } from 'vue'
import { preferenceApi } from '../api/preferences'
import { ElMessage } from 'element-plus'

export default {
  name: 'DataSettings',
  setup() {
    const form = ref({ dataPath: '', attachmentPath: '' })
    const saving = ref(false)
    const saved = ref(false)

    onMounted(async () => {
      try {
        const res = await preferenceApi.get()
        if (res.data) {
          form.value.dataPath = res.data.dataPath || ''
          form.value.attachmentPath = res.data.attachmentRootPath || ''
        }
      } catch (e) { /* use defaults */ }
    })

    async function save() {
      saving.value = true
      saved.value = false
      try {
        await preferenceApi.update({
          dataPath: form.value.dataPath || undefined,
          attachmentRootPath: form.value.attachmentPath || undefined
        })
        saved.value = true
        ElMessage.success('已保存，部分修改需重启后生效')
      } catch (e) {
        ElMessage.error('保存失败')
      } finally { saving.value = false }
    }

    return { form, saving, saved, save }
  }
}
</script>
