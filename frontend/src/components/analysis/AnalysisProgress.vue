<template>
  <div v-if="phase === 'analyzing'" class="analysis-progress">
    <el-progress :percentage="progressPercent" :stroke-width="6" />
    <div class="analysis-progress__info">
      <span class="analysis-progress__stage">{{ stageLabel }}</span>
      <span class="analysis-progress__target">{{ currentTarget }}</span>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'AnalysisProgress',
  props: {
    phase: String,
    currentStage: String,
    currentTarget: String
  },
  setup(props) {
    const stageLabel = computed(() => {
      switch (props.currentStage) {
        case 'ANALYSIS': return '正在分析项目结构…'
        case 'KNOWLEDGE': return '正在提取知识点…'
        default: return '正在分析…'
      }
    })
    const progressPercent = computed(() => {
      switch (props.currentStage) {
        case 'ANALYSIS': return 60
        case 'KNOWLEDGE': return 50
        default: return 30
      }
    })
    return { stageLabel, progressPercent }
  }
}
</script>

<style scoped>
.analysis-progress {
  margin: 20px 0;
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: 8px;
}
.analysis-progress__info {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
