<template>
  <el-card class="llm-settings">
    <template #header><span>大模型配置</span></template>
    <el-form :model="form" label-width="100px">
      <el-form-item label="提供商">
        <el-select v-model="form.provider" style="width:100%">
          <el-option label="OpenAI" value="OPENAI" />
          <el-option label="Anthropic" value="ANTHROPIC" />
          <el-option label="Ollama (本地)" value="OLLAMA" />
          <el-option label="DeepSeek" value="DEEPSEEK" />
          <el-option label="通义千问" value="QWEN" />
          <el-option label="自定义兼容" value="CUSTOM" />
        </el-select>
      </el-form-item>
      <el-form-item label="API Key">
        <el-input v-model="form.apiKey" type="password" show-password placeholder="sk-..." />
      </el-form-item>
      <el-form-item label="Base URL">
        <el-input v-model="form.baseUrl" placeholder="可选，留空使用默认" />
      </el-form-item>
      <el-form-item label="模型">
        <el-input v-model="form.model" placeholder="gpt-4o / claude-sonnet-4-6 / llama3" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="save">保存配置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script>
import { ref, onMounted } from 'vue'
import { llmApi } from '../api/llm'
import { ElMessage } from 'element-plus'

export default {
  name: 'LLMSettings',
  setup() {
    const form = ref({ provider: 'OPENAI', apiKey: '', baseUrl: '', model: 'gpt-4o' })

    onMounted(async () => {
      try {
        const res = await llmApi.getConfig()
        const data = res.data
        form.value.provider = data.provider
        form.value.baseUrl = data.baseUrl || ''
        form.value.model = data.model
      } catch (e) {
        // use defaults
      }
    })

    async function save() {
      try {
        await llmApi.updateConfig({
          provider: form.value.provider,
          apiKey: form.value.apiKey || undefined,
          baseUrl: form.value.baseUrl || undefined,
          model: form.value.model
        })
        ElMessage.success('配置已保存')
      } catch (e) {
        ElMessage.error('保存失败')
      }
    }

    return { form, save }
  }
}
</script>
