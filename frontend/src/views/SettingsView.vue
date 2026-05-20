<template>
  <div class="settings-view">
    <h3>设置</h3>
    <LLMSettings />
    <DataSettings />
  </div>
</template>

<script>
import LLMSettings from '../components/LLMSettings.vue'
import DataSettings from '../components/DataSettings.vue'

export default {
  name: 'SettingsView',
  components: { LLMSettings, DataSettings }
}
</script>

<style scoped>
.settings-view {
  padding: 24px;
  max-width: 720px;
  margin: 0 auto;
  height: 100%;
  overflow-y: auto;
}
.settings-view h3 {
  margin: 0 0 20px;
  font-size: 18px;
}
</style>
