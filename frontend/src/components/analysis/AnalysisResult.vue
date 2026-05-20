<template>
  <div v-if="content" class="analysis-result">
    <div class="analysis-result__content" v-html="renderedMarkdown"></div>
  </div>
</template>

<script>
import { computed } from 'vue'
import { marked } from 'marked'

export default {
  name: 'AnalysisResult',
  props: {
    content: { type: String, default: '' }
  },
  setup(props) {
    const renderedMarkdown = computed(() => {
      if (!props.content) return ''
      return marked.parse(props.content)
    })
    return { renderedMarkdown }
  }
}
</script>

<style scoped>
.analysis-result {
  margin-top: 20px;
}
.analysis-result__content {
  padding: 20px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  line-height: 1.7;
}
.analysis-result__content :deep(h2) {
  margin-top: 24px;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 8px;
}
.analysis-result__content :deep(code) {
  background: var(--bg-secondary);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}
.analysis-result__content :deep(pre) {
  background: var(--bg-secondary);
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
}
</style>
